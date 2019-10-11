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

app.post('/getItems', function(req, res) {
    console.log("request gotten")

    var major = req.body.major
    var minor = req.body.minor
    var region = ''

    console.log(major)
    console.log(minor)

    /*
      grocery: 15212, 31506
      lifestyle: 30462, 43265
      produce: 26535, 44799
    */

    if (major == '15212' && minor == '31506') {
        region = 'grocery'
    } else if (major == '30462' && minor == '43265') {
        region = 'lifestyle'
    } else if (major == '26535' && minor == '44799') {
        region = 'produce'
    }

    var itemsQuery = `SELECT * FROM items WHERE region="${region}"`

    con.query(itemsQuery, function(err, result, fields) {
        if (err) {
            console.log(err)
            res.status(500)
            res.send()
        } else {

            console.log(result)
            res.status(200)
            res.setHeader('Content-Type', 'application/json')
            res.json(result)




            /*
            var usernameJson = {
                'username': username
            }

            //create a jwt token with the username of the user
            var token = jwt.sign(JSON.stringify(usernameJson), jwtSecret)

            //send token back to the app as json object
            var jwtJson = {
                'token': token
            }

            res.status(200)
            res.setHeader('Content-Type', 'application/json')
            res.json(jwtJson)
            */
        }
    })
})

app.listen(PORT, () => {
    console.log(`Our app is running on port ${ PORT }`);
})
