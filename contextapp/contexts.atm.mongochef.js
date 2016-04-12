use Contexts;
db.createCollection("atms");
db.atms.insertMany([{ 
    "_id" : ObjectId("56eee70305c7d0183cfa316f"), 
    "lat" : 33.643073, 
    "lng" : 72.988199, 
    "place" : "SEECS Cafe-C2", 
    "bank_name" : "Habib Bank Limited"
},
{ 
    "_id" : ObjectId("56eee7f305c7d0183cfa3173"), 
    "lat" : 33.6433345, 
    "lng" : 72.9848829, 
    "place" : "HBL NUST Branch Sector H-12", 
    "bank_name" : "Habib Bank Limited"
},
{ 
    "_id" : ObjectId("56eee9bc05c7d0183cfa3175"), 
    "lat" : 33.6462545, 
    "lng" : 72.9900225, 
    "place" : "HBL ATM C1", 
    "bank_name" : "Habib Bank Limited"
},
{ 
    "_id" : ObjectId("56eeea9b05c7d0183cfa3177"), 
    "lat" : 33.6453463, 
    "lng" : 72.975944, 
    "place" : "PSO Petrol Pump Kashmir Highway near NUST", 
    "bank_name" : "Allied Bank Limited"
}]
);