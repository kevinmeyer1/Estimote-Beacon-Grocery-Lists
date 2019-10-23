# InClass05 Android App with API and Database

This project includes the API and Android app needed to complete the requirements of InClass05.

The API was create in Node.js and uses Express for the routing. The data is stored on an Amazon AWS MySQL database.

The API is hosted on Heroku on the web address: `https://inclass05.herokuapp.com/`.

All image files needed for this assignment are on Heroku and can be accessed from the app using URLs. Example: `https://inclass05.herokuapp.com/milk.jpg`

## Routes

```/getItems```:

    https://inclass03.herokuapp.com/getItems

This is the API route to get items from the database used to populate the list on the app. The route takes in a json object in the form:

    {
        "major": "15212",
        "minor": "31506"
    }

The major and minor are used to figure out which beacon region (grocery, lifestyle, or produce) is being used. In this example, the major and minor given determine that the beacon located is in the grocery section. The database is connected to, the items are grabbed, and all the data is returned as a json object. Example return data looks like:

    [
        {
            "discount": 20,
            "name": "Brach's Jelly Beans",
            "photo": "jelly_beans.png",
            "price": 2.21,
            "region": "grocery"
        },
        {
            "discount": 15,
            "name": "Coca Cola",
            "photo": "coca_cola.png",
            "price": 6.99,
            "region": "grocery"
        },
        {
            "discount": 15,
            "name": "Cranberry Cocktail",
            "photo": "cranberry_cocktail.png",
            "price": 1.89,
            "region": "grocery"
        },
        {
            "discount": 10,
            "name": "Croissants",
            "photo": "croissants.png",
            "price": 2.79,
            "region": "grocery"
        },
        {
            "discount": 10,
            "name": "Gatorade",
            "photo": "gatorade.png",
            "price": 3.89,
            "region": "grocery"
        },
        {
            "discount": 15,
            "name": "HI-C Fruit Punch",
            "photo": "hi_c_fruit_punch.png",
            "price": 4.67,
            "region": "grocery"
        },
        {
            "discount": 20,
            "name": "Milk",
            "photo": "milk.jpg",
            "price": 10.5,
            "region": "grocery"
        }
    ]



## Database Schema

The MySQL database on Amazon AWS is pretty basic.

| discount | name | photo | price | region |
|----------|------|-------|-------|--------|

`Discount` is an integer. `name`, `photo`, and `region` are VARCHARs. `price` is a double. The primary key is `name`.

## Images of the app

### All Items

https://imgur.com/a/tnvUf1e

### Grocery

https://imgur.com/a/cZp8hQp

## Avoiding Oscillation

Every time `onBeaconsDiscovered` is called, the first useable beacon is added to an array. When the array of 5 beacons is filled up, the app checks which beacon appears most frequently, then uses that beacons major and minor to get a list from the API. After the array is full and another closest beacon comes in, the oldest beacon in the array is removed, everything is shifted down, and the newest beacon is put in the last spot. The check for frequency is done again and either a new list is shown or nothing happens. This helps cut down oscillation by averaging the closest beacon. It will not fix it, but it does help.
