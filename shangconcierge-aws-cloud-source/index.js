// Example express application adding the parse-server module to expose Parse
// compatible API routes.

var express = require('express');
var ParseServer = require('parse-server').ParseServer;

var databaseUri = process.env.DATABASE_URI || process.env.MONGOLAB_URI

if (!databaseUri) {
  console.log('DATABASE_URI not specified, falling back to localhost.');
}
var api = new ParseServer({
  databaseURI: databaseUri || 'mongodb://adm_sc:oioi@ds019078.mlab.com:19078/scdemo',
  cloud: process.env.CLOUD_CODE_MAIN || '../main.js',
  appId: process.env.APP_ID || 'xIVzI9p5yVJPBpf1BjgnyCYSXiD0E5qZvelOSfKb',
  masterKey: process.env.MASTER_KEY || 'bJrGtRCU6hY4Fe6MQXV1arL097j6TPzmNLwOzIik'
});
//var api = new ParseServer({
//  databaseURI: databaseUri || 'mongodb://adm_sc:oioi@ds019078.mlab.com:19078/scdemo',
//  cloud: process.env.CLOUD_CODE_MAIN || __dirname + '/cloud/main.js',
//  appId: process.env.APP_ID || 'xIVzI9p5yVJPBpf1BjgnyCYSXiD0E5qZvelOSfKb',
//  masterKey: process.env.MASTER_KEY || 'bJrGtRCU6hY4Fe6MQXV1arL097j6TPzmNLwOzIik'
//});
// Client-keys like the javascript key or the .NET key are not necessary with parse-server
// If you wish you require them, you can set them as options in the initialization above:
// javascriptKey, restAPIKey, dotNetKey, clientKey

var app = express();

// Serve the Parse API on the /parse URL prefix
var mountPath = process.env.PARSE_MOUNT || '/parse';
app.use(mountPath, api);

// Parse Server plays nicely with the rest of your web routes
app.get('/', function(req, res) {
  res.status(200).send('I dream of being a web site.');
});

var port = process.env.PORT || 1337;
app.listen(port, function() {
    console.log('parse-server-example running on port ' + port + '.');
});
