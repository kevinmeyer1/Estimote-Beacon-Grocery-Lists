const express = require('express')
const app = express()
const mysql = require('mysql')
const bodyParser = require('body-parser')
const config = require('./config.json')

var mySQLUsername = config['mySQLUsername']
var mySQLPassword = config['mySQLPassword']
var mySQLHost = config['mySQLHost']
var mySQLPort = config['mySQLPort']
var mySQLDatabase = config['mySQLDatabase']

//Heroku code to set the listening port
const PORT = process.env.PORT || 3000

// ------------------------------------------------- CONNECTIONS  -------------------------------------------------

var con = mysql.createConnection({
    host: mySQLHost,
    user: mySQLUsername,
    password: mySQLPassword,
    database: mySQLDatabase
})

app.use(bodyParser.json())

//Allows the app to look at (example) inclass05.herokuapp.com/coca_cola.png and get the static file (image) in the assets folder.
app.use(express.static('assets'))

//Takes in a major/minor, uses to determine the section of the store the user is in. Grabs the jsonArray of items and sends it back as part of the response
app.post('/getItems', function(req, res) {
    console.log("request gotten")

    var major = req.body.major
    var minor = req.body.minor
    var region = ''

    console.log(major)
    console.log(minor)

    /*
        Values for class:

        grocery: 15212, 31506
        lifestyle: 30462, 43265
        produce: 26535, 44799

        Our beacons:

         major=47152, minor=61548
         major=49357, minor=20877
    */

    if (major == '49357' && minor == '20877') {
        region = 'grocery'
    } else if (major == '30462' && minor == '43265') {
        region = 'lifestyle'
    } else if (major == '47152' && minor == '61548') {
        region = 'produce'
    } else {
        region = null

        res.status(400)
        res.setHeader('Content-Type', 'text/plain')
        res.write('Major and minor do not match to a region')
        res.send()
    }

    var itemsQuery = `SELECT * FROM items WHERE region="${region}"`

    con.query(itemsQuery, function(err, result, fields) {
        if (err) {
            console.log(err)
            res.status(500)
            res.setHeader('Content-Type', 'text/plain')
            res.write(`Error while getting items from database for region: ${region}`)
            res.send()
        } else {
            //JSONObject is sent back from db.
            res.status(200)
            res.setHeader('Content-Type', 'application/json')
            res.json(result)
        }
    })
})

app.listen(PORT, () => {
    console.log(`Our app is running on port ${ PORT }`);
})
