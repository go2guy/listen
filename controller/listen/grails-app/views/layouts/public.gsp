<!DOCTYPE html>
<html>
  <head>
    <title>Listen - <g:layoutTitle/> - Interact Incorporated</title>
    <link rel="shortcut icon" href="${resource(dir: 'resources/app/images', file: 'favicon.ico')}">
    <link rel="stylesheet" href="${resource(dir: 'resources/yui-2.8.0r4', file: 'reset-fonts.css')}">
    <link rel="stylesheet" href="${resource(dir: 'resources/app/css', file: 'common.css')}">
    <style type="text/css">
.fixed-width {
    display: block;
    margin-left: auto;
    margin-right: auto;
    width: 950px;
}

#container {
    margin: 0 auto;
    padding-top: 20px;
}

#banner {
    height: 41px;
    background-color: #E4F0FB;
    border-bottom: 2px solid #80B5ED;
    box-shadow: 0 3px 5px #DDD;
}

#banner .fixed-width {
    background: #E4F0FB url('${g.resource(dir: 'resources/app/images', file: 'listen_logo_50x24.png')}') left 8px no-repeat;
    height: 41px;
}

#banner h1 {
    color: #054B7A;
    float: right;
    font-size: 18px;
    font-weight: bold;
    margin-right: 10px;
    margin-top: 18px;
}
    </style>
    <g:layoutHead/>
  </head>
  <body>
    <div id="banner">
      <div class="fixed-width">
        <h1>Checkout</h1>
      </div>
    </div>
    <div id="container" class="fixed-width">
      <g:layoutBody/>
      <div id="footer">
        <listen:copyright/>
      </div>
    </div>
  </body>
</html>