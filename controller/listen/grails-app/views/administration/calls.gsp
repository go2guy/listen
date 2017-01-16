<html>
  <head>
    <title>Calls In Progress</title>
    <meta name="layout" content="main"/>
    <meta name="tab" content="administration"/>
    <style type="text/css">
#calls-list {
    border: 1px solid #CBC9D6;
    margin: 0;
    padding: 0;
}

#calls-list > li {
    cursor: move;
    display: block;
    height: 50px;
    margin: 0;
    padding: 0;
    width: 924px;
}

#calls-list > li.placeholder {
    background-color: #6DACD6;
    border-color: #6DACD6;
}

#calls-list > li > canvas {
    display: inline-block;
    margin: 0;
    padding: 0;
}
    </style>
  </head>
  <body>
    <ul id="calls-list"></ul>
    <script type="text/javascript">

function Graph(options) {
    options = options || {};
    options.secondsPerPixel = options.secondsPerPixel || 1.94;
    options.fps = options.fps || 10;
    options.grid = options.grid || { lineWidth: 1, secondsPerLine: 60 };
    options.rightOffset = 200;
    this.options = options;

    this.active = true;
    this.calls = [];
}

Graph.prototype.streamTo = function(canvas, delay) {
    var self = this;
    (function render() {
        self.render(canvas, new Date().getTime() - (delay || 0));
        if(self.active === true) {
            setTimeout(render, 1000 / self.options.fps);
        }
    })();
};

Graph.prototype.destroy = function() {
    this.active = false;
};

Graph.prototype.render = function(canvas, time) {
    var context = canvas.getContext('2d');
    var options = this.options;
    var dimensions = { top: 0, left: 0, width: canvas.clientWidth, height: canvas.clientHeight };

    context.save();

    time = time - time % options.secondsPerPixel;

    context.translate(dimensions.left, dimensions.top);

    context.beginPath();
    context.rect(0, 0, dimensions.width, dimensions.height);
    context.clip();

    context.save();
    context.fillStyle = '#FAFAFA';
    context.fillRect(0, 0, dimensions.width, dimensions.height);
    context.restore();

    context.save();
    context.lineWidth = options.grid.lineWidth || 1;
    context.strokeStyle = '#EEEEEE';

    if(options.grid.secondsPerLine > 0) {
        for(var t = dimensions.width - options.rightOffset, label = 0; t >= 0; t -= (options.grid.secondsPerLine / options.secondsPerPixel), label++) {
            if(label % 5 == 0) {
                context.fillStyle = '#CBC9D6';
                context.font = 'normal 10px sans-serif';
                context.fillText(label, t + 2, 10);
            }

            context.beginPath();
            context.moveTo(t, 0);
            context.lineTo(t, dimensions.height);
            context.stroke();
            context.closePath();
        }
    }

    context.lineWidth = 20;
    for(var d = 0; d < this.calls.length; d++) {
        var call = this.calls[d];

        if(d === 0) {
            context.fillStyle = '#000000';
            context.font = 'normal 20px sans-serif';
            context.fillText(call.ani, dimensions.width - (options.rightOffset - 100), 30);
        }

        var x0 = dimensions.width - options.rightOffset - (1 / (options.secondsPerPixel / call.started));
        var x1 = call.ended == 0 ? dimensions.width - options.rightOffset : dimensions.width - options.rightOffset - (1 / (options.secondsPerPixel / call.ended));

        context.strokeStyle = call.ended > 0 ? '#82BA7D' : '#0A7D00';

        context.beginPath();
        context.moveTo(x0, 25);
        context.lineTo(x1, 25);
        context.stroke();
        context.closePath();

        if(call.ended == 0) {
            var elapsed = call.started - call.ended;
            var mins = Math.floor(elapsed / 60);
            var secs = Math.floor(elapsed % 60);
            var formatted = mins + ':' + (secs < 10 ? '0' + secs : secs) + '  >  ' + call.dnis;
            context.fillStyle = '#0A7D00';
            context.font = 'normal 10px sans-serif';
            context.fillText(formatted, dimensions.width - (options.rightOffset - 5), 30);
        }
    }

    context.restore();
};

function poll(moveRows) {
    $.ajax({
        url: '${createLink(action: 'callsData', mapping: 'internalApi')}',
        dataType: 'json',
        cache: false,
        success: function(data) {
            var received = [];

            for(var i = 0; i < data.length; i++) {
                var call = data[i];
                var key = 'ani' + call.ani;
                received.push(key);

                if(!(key in graphs)) {
                    graphs[key] = new Graph();
                    $('#calls-list').prepend('<li><canvas id="' + key + '" width="924" height="50"></canvas></li>');
                    graphs[key].streamTo(document.getElementById(key));
                }

                var existingCall = false;
                for(var j = 0; j < graphs[key].calls.length; j++) {
                    if(graphs[key].calls[j].sessionId === call.sessionId) {
                        existingCall = graphs[key].calls[j];
                    }
                }

                if(existingCall === false) {
                    graphs[key].calls.push(call)
                    if(moveRows) {
                        var list = $('#calls-list > li');
                        if(list.size() > 1) {
                            $('#calls-list li:first').before($('#' + key).parent());
                        }
                    }
                } else {
                    existingCall.started = call.started;
                    existingCall.ended = call.ended;
                }
            }

            for(var key in graphs) {
                if(graphs.hasOwnProperty(key)) {
                    var found = false;
                    for(var i = 0; i < received.length; i++) {
                        if(received[i] === key) found = true;
                    }

                    if(!found) {
                        graphs[key].destroy;
                        delete graphs[key];
                        $('#' + key).parent().remove();
                    }
                }
            }
        }
    });
}

var graphs = {};

$(document).ready(function() {
    $('#calls-list').sortable({
        opacity: .5,
        placeholder: 'placeholder',
        axis: 'y',
        tolerance: 'pointer'
    }).disableSelection();

    poll(false);
    setInterval(function() {
        poll(true);
    }, 1000);
});

    </script>
  </body>
</html>