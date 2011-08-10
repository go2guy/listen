<html>
  <head>
    <title>Unknown Error</title>
    <meta name="layout" content="main"/>
    <style>
.error-image,
.error-details {
    display: block;
    float: left;
    margin-bottom: 20px;
    margin-top: 20px;
}

.error-image {
    margin-left: 100px;
}

.error-details {
    margin-left: 20px;
}

h2, h4 {
    margin: 0 0 10px 0;
    padding: 0;
}

h4 { font-weight: normal; }

    </style>
  </head>
  <body>
    <img class="error-image" src="${resource(dir: 'resources/app/images', file: 'sad.jpg')}"/>
    <div class="error-details">
      <h2>We seem to have encountered an error.</h2>
      <h4>The error has been logged. Contact your system administrator for assistance.</h4>
    </div>
  </body>
</html>