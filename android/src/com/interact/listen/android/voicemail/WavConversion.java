package com.interact.listen.android.voicemail;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class WavConversion
{
    public interface OnProgressUpdate
    {
        void onProgressUpdate(int percent);
    }

    private static final String TAG = Constants.TAG + "WavConvert";
    
    
    public static long copyToLinear(InputStream in, OutputStream out, long downloadSize,
                                    OnProgressUpdate listener) throws IOException
    {
        WavInputStream wInput = new WavInputStream(downloadSize, listener, in);

        if(!wInput.initHead())
        {
            return wInput.write(out);
        }

        int chunkSize = wInput.getInt();

        if(!wInput.checkAndIncrement("WAVEfmt "))
        {
            Log.i(TAG, "header doesn't have WAVE followed by fmt");
            return wInput.write(out);
        }

        int subChunk1Size = wInput.getInt();
        short audioFormat = wInput.getShort();
        short numChannels = wInput.getShort();
        int sampleRate = wInput.getInt();
        int byteRate = wInput.getInt();
        short blockAlign = wInput.getShort();
        short bitsPerSample = wInput.getShort();

        if(Log.isLoggable(TAG, Log.INFO))
        {
            Log.i(TAG, "Download Size: " + downloadSize);
            Log.i(TAG, "ChunkSize: " + chunkSize);
            Log.i(TAG, "SubChunk1Size: " + subChunk1Size);
            Log.i(TAG, "AudioFormat: " + audioFormat);
            Log.i(TAG, "NumChannels: " + numChannels);
            Log.i(TAG, "SampleRate: " + sampleRate);
            Log.i(TAG, "ByteRate: " + byteRate);
            Log.i(TAG, "BlockAlign: " + blockAlign);
            Log.i(TAG, "BitsPerSample: " + bitsPerSample);
        }
        
        if(subChunk1Size != 16 || (audioFormat != 6 && audioFormat != 7) ||
            bitsPerSample != 8 || numChannels != 1)
        {
            // require PCM, A-Law or Mu-Law, 8-bits, mono
            Log.i(TAG, "WAV format conversion not supported");
            return wInput.write(out);
        }

        if(!wInput.checkAndIncrement("fact") || wInput.getInt() != 4)
        {
            Log.i(TAG, "compressed WAV missing fact section");
            return wInput.write(out);
        }

        int factSize = wInput.getInt();
        
        if(!wInput.checkAndIncrement("data"))
        {
            Log.i(TAG, "WAV missing data section or has unsupported headers");
            return wInput.write(out);
        }

        int subChunk2Size = wInput.getInt();

        if(Log.isLoggable(TAG, Log.INFO))
        {
            Log.i(TAG, "SubChunk2Size: " + subChunk2Size);
            Log.i(TAG, "fact size: " + factSize);
        }
        
        if(factSize != subChunk2Size)
        {
            Log.i(TAG, "fact and data size different " + factSize + " " + subChunk2Size);
            return wInput.write(out);
        }
        
        if(downloadSize != chunkSize + 8)
        {
            Log.i(TAG, "download size isn't as expected " + downloadSize + " " + chunkSize + "+8");
        }

        int newDataSize = subChunk2Size * 2;
        
        WavOutputStream wOutput = new WavOutputStream(out);
        wOutput.writeString("RIFF");
        wOutput.writeInt(newDataSize + 36);
        wOutput.writeString("WAVE");
        wOutput.writeString("fmt ");
        wOutput.writeInt(16); // sub chunk 1 size
        wOutput.writeShort((short)1); // pcm
        wOutput.writeShort(numChannels);
        wOutput.writeInt(sampleRate);
        wOutput.writeInt(byteRate * 2);
        wOutput.writeShort((short)(blockAlign * 2));
        wOutput.writeShort((short)16); // bits
        wOutput.writeString("data");
        wOutput.writeInt(newDataSize);
        wOutput.flush();
        
        LogToLinear converter = audioFormat == 6 ? new ALawToLinear() : new MuLawToLinear();
        return wInput.write(wOutput, converter);
    }

    private static class WavInputStream
    {
        private static final int LOG_HEAD_SIZE = 56;

        private byte[] buffer;
        private ByteBuffer byteBuffer;
        private long downloadSize;
        private OnProgressUpdate listener;
        private int lastPercent;
        private InputStream in;
        private int total;
        
        public WavInputStream(long expectedByteSize, OnProgressUpdate l, InputStream i)
        {
            buffer = new byte[1024];
            byteBuffer = ByteBuffer.wrap(buffer);
            downloadSize = expectedByteSize;
            listener = l;
            lastPercent = 0;
            in = i;
            total = 0;
        }

        private void updateProgress()
        {
            if(listener != null && downloadSize > 0)
            {
                int percent = (int)(total * 100L / downloadSize);
                if(percent > lastPercent)
                {
                    lastPercent = percent;
                    listener.onProgressUpdate(percent);
                }
                
            }
        }
        public boolean initHead() throws IOException
        {
            byteBuffer.clear();

            total = 0;
            int read = 0;
            while((read = in.read(buffer, total, LOG_HEAD_SIZE - total)) != -1)
            {
                total += read;
                if(total >= LOG_HEAD_SIZE)
                {
                    break;
                }
            }
            
            updateProgress();
            byteBuffer.limit(total);
            
            if(total < LOG_HEAD_SIZE)
            {
                return false;
            }
            
            if(!checkAndIncrement("RIF"))
            {
                return false;
            }
            
            byte oByte = byteBuffer.get();
            if(oByte != 'F' && oByte != 'X')
            {
                return false;
            }
            byteBuffer.order(oByte == 'F' ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN);

            return true;
        }
        
        public long write(OutputStream out) throws IOException
        {
            out.write(buffer, 0, byteBuffer.limit());
            byteBuffer.clear();
            
            int read = 0;
            while ((read = in.read(buffer)) != -1)
            {
                out.write(buffer, 0, read);
                total += read;
                if(isInterrupted() && total != downloadSize)
                {
                    return -1;
                }
                if(total > downloadSize)
                {
                    downloadSize = total;
                }
                updateProgress();
            }
            return total;
        }
        
        public long write(WavOutputStream out, LogToLinear convert) throws IOException
        {
            byteBuffer.clear();
            
            int read = 0;
            while ((read = in.read(buffer)) != -1)
            {
                for(int i = 0; i < read; ++i)
                {
                    out.writeShort(convert.convert(buffer[i]));
                }
                total += read;
                if(isInterrupted() && total != downloadSize)
                {
                    return -1;
                }
                updateProgress();
            }
            out.flush();
            return total;
        }

        public int getInt()
        {
            return byteBuffer.getInt();
        }
        
        public short getShort()
        {
            return byteBuffer.getShort();
        }

        public boolean checkAndIncrement(String value)
        {
            for(int i = 0; i < value.length(); ++i)
            {
                if(value.charAt(i) != byteBuffer.get())
                {
                    return false;
                }
            }
            return true;
        }
        
    }
    
    private static class WavOutputStream
    {
        private OutputStream out;
        private ByteBuffer byteBuffer;
        
        public WavOutputStream(OutputStream o)
        {
            out = o;
            byteBuffer = ByteBuffer.allocate(2048);
            byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        }
        
        public void flush() throws IOException
        {
            if(byteBuffer.position() > 0)
            {
                out.write(byteBuffer.array(), 0, byteBuffer.position());
                byteBuffer.clear();
            }
        }
        
        public void writeString(String str) throws IOException
        {
            if(byteBuffer.remaining() < str.length())
            {
                flush();
            }
            for(int i = 0; i < str.length(); ++i)
            {
                byteBuffer.put((byte)str.charAt(i));
            }
        }
        
        public void writeInt(int v) throws IOException
        {
            if(byteBuffer.remaining() < 4)
            {
                flush();
            }
            byteBuffer.putInt(v);
        }
        
        public void writeShort(short v) throws IOException
        {
            if(byteBuffer.remaining() < 4)
            {
                flush();
            }
            byteBuffer.putShort(v);
        }

    }
    
    private interface LogToLinear
    {
        short convert(byte src);
    }

    private static class ALawToLinear implements LogToLinear
    {
        @Override
        public short convert(byte src)
        {
            int aval = (src ^ 0x00000055) & 0x000000FF;

            int t = ((aval & 0x0000000F) << 4) | 0x00000008;

            int seg = (aval & 0x00000070) >> 4;
            if(seg > 0)
            {
                t += 0x00000100;
                if((t & 0xFFFF0000) != 0)
                {
                    t = 0x00000009;
                }
                if(seg > 1)
                {
                    t <<= seg - 1;
                    t &= 0x0000FFFF;
                }
            }

            if((aval & 0x00000080) == 0)
            {
                t = -t;
            }

            return (short)t;
        }
    }

    private static class MuLawToLinear implements LogToLinear
    {
        @Override
        public short convert(byte src)
        {
            int uval = ~src & 0x000000FF;

            int t = ((uval & 0x0000000F) << 3) + 0x00000084;
            t <<= (uval & 0x00000070) >> 4;

            if((uval & 0x00000080) != 0)
            {
                t = 0x00000084 - t;
            }
            else
            {
                t -= 0x00000084;
            }

            return (short)t;
        }
    }

    private static boolean isInterrupted()
    {
        return Thread.currentThread().isInterrupted();
    }

    private WavConversion()
    {
    }
}
