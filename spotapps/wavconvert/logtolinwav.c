
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <strings.h>
#include <stdint.h>

#define PCM_FORMAT (1)
#define ALAW_FORMAT (6)
#define MULAW_FORMAT (7)

#pragma pack(2)

typedef struct
{
    char chunkID[4];
    unsigned int chunkSize;
    char format[4];
    char subChunk1ID[4];
    int subChunk1Size;
    short audioFormat;
    short numChannels;
    int sampleRate;
    int byteRate;
    short blockAlign;
    short bitsPerSample;
} WavHeader;

typedef struct
{
    char format[4];
    unsigned int size;
    unsigned int value;
} FactSection;

typedef struct
{
    char format[4];
    unsigned int dataSize;
} DataSection;

#pragma pack(0)

size_t readIn(void* data, size_t size);
int writeOut(const void* data, size_t size);
int writeAll(const void* data, size_t size);
int writeError(const void* data, size_t size, const char* message);

typedef uint16_t (*CONVERT_FUNC)(uint8_t);

uint16_t convertAlaw(uint8_t aval);
uint16_t convertMuLaw(uint8_t uval);

int main(int argc, char* argv[]) {
    if (argc != 1) {
        fprintf(stderr,"Usage: %s < [8-bit A-Law or Mu-Law WAVE] > [16-bit PCM WAVE output file]\n",argv[0]);
        return 0;
    }

    uint8_t inBuffer[1024];

    WavHeader* inHeader = (WavHeader*)inBuffer;
    FactSection* inFact = (FactSection*)(inBuffer + sizeof(WavHeader));
    DataSection* inData = (DataSection*)(inBuffer + sizeof(WavHeader) + sizeof(FactSection));

    const size_t MIN_HEADER = sizeof(WavHeader) + sizeof(FactSection) + sizeof(DataSection);

    size_t inRead = readIn(inBuffer,MIN_HEADER);
    if (inRead < MIN_HEADER)
        return writeOut(inBuffer,inRead);

    if (strncmp(inHeader->chunkID,"RIFF",4) || strncmp("WAVE",inHeader->format,4))
        return writeAll(inBuffer,inRead);

    if (strncmp("fmt ",inHeader->subChunk1ID,4) || inHeader->subChunk1Size < 16)
        return writeError(inBuffer,inRead,"invalid format section");

    if (inHeader->audioFormat != ALAW_FORMAT && inHeader->audioFormat != MULAW_FORMAT)
        return inHeader->audioFormat == PCM_FORMAT ? writeAll(inBuffer,inRead) : writeError(inBuffer,inRead,"unknown audio format");

    if (inHeader->bitsPerSample != 8)
        return writeError(inBuffer,inRead,"on 8-bit to 16-bit conversion supported");

    size_t extraRead = inHeader->subChunk1Size <= 16 ? 0 : inHeader->subChunk1Size - 16;
    if (extraRead > 0) {
        size_t nRead = readIn(inBuffer + inRead,extraRead);
        inRead += nRead;
        if (nRead < extraRead)
            return writeOut(inBuffer,inRead);
        inFact = (FactSection*)(((char*)inFact) + extraRead);
        inData = (DataSection*)(((char*)inData) + extraRead);
    }

    if (strncmp("fact",inFact->format,4) || inFact->size != 4)
        return writeError(inBuffer,inRead,"missing 'fact' section");

    if (strncmp("data",inData->format,4))
        return writeError(inBuffer,inRead,"unknown sections or missing data section");

    if (inFact->value != inData->dataSize)
        return writeError(inBuffer,inRead,"fact value doesn't match data size");

    if (inData->dataSize != inHeader->chunkSize + 8 - (MIN_HEADER + extraRead))
        return writeError(inBuffer,inRead,"chunk size and data size are inconsistent");

    uint16_t outBuffer[1024];
    const size_t OUT_HEADER = sizeof(WavHeader) + sizeof(DataSection);

    memset(outBuffer,0,OUT_HEADER);

    WavHeader* outHeader = (WavHeader*)outBuffer;
    DataSection* outData = (DataSection*)(((char*)outBuffer) + sizeof(WavHeader));

    strncpy(outHeader->chunkID,"RIFF",4);
    outHeader->chunkSize = inData->dataSize * 2 + sizeof(WavHeader);
    strncpy(outHeader->format,"WAVE",4);
    strncpy(outHeader->subChunk1ID,"fmt ",4);
    outHeader->subChunk1Size = 16;
    outHeader->audioFormat = PCM_FORMAT;
    outHeader->numChannels = inHeader->numChannels;
    outHeader->sampleRate = inHeader->sampleRate;
    outHeader->byteRate = inHeader->byteRate * 2;
    outHeader->blockAlign = inHeader->blockAlign * 2;
    outHeader->bitsPerSample = 16;

    strncpy(outData->format,"data",4);
    outData->dataSize = inData->dataSize * 2;

    writeOut(outBuffer,OUT_HEADER);

    CONVERT_FUNC cfunc = inHeader->audioFormat == ALAW_FORMAT ? convertAlaw : convertMuLaw;

    int read = 0;
    int i;
    while ((read = readIn(inBuffer,sizeof(inBuffer))) > 0) {
        for (i = 0; i < read; ++i)
            outBuffer[i] = cfunc(inBuffer[i]);

        writeOut(outBuffer,read<<1);

        if (read < (int)sizeof(inBuffer))
            break;
    }
    fflush(stdout);

    return 0;
}

size_t readIn(void* data, size_t size) {
    size_t r = fread(data,1,size,stdin);
    if (r < size && ferror(stdin))
    {
        perror("reading");
        exit(1);
    }
    return r;
}

int writeOut(const void* data, size_t size) {
    if (fwrite(data,size,1,stdout) < 1) {
        perror("writing");
        exit(1);
    }
    return 0;
}

int writeError(const void* data, size_t size, const char* message) {
    fprintf(stderr,"%s\n",message);
    return writeAll(data,size);
}

int writeAll(const void* data, size_t size) {
    writeOut(data,size);

    char buffer[10240];
    int read = 0;
    while ((read = fread(buffer,1,sizeof(buffer),stdin)) > 0) {
        if (fwrite(buffer,read,1,stdout) < 1) {
            perror("writing");
            return 1;
        }

        if (read < (int)sizeof(buffer))
            break;
    }
    fflush(stdout);

    if (ferror(stdin)) {
        perror("reading");
        return 1;
    }
    return 0;
}

uint16_t convertAlaw(uint8_t aval) {
    aval ^= 0x55;

    uint16_t t = ((aval & 0x0F) << 4) | 0x08;
    short seg = (aval & 0x70) >> 4;
    if (seg > 0) {
        t += 0x0100;
        if (seg > 1)
            t <<= seg - 1;
    }

    return (aval & 0x80) ? -t : t;
}

uint16_t convertMuLaw(uint8_t uval) {
    uval = ~uval;
    uint16_t t = ((uval & 0x0F) << 3) + 0x0084;
    t <<= (uval & 0x70) >> 4;
    return (uval & 0x80) ? 0x0084 - t : t - 0x0084;
}
