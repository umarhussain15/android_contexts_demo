<?php
// all the dependencies required to use connect to MongoDB
require 'vendor/autoload.php';
use MongoDB\Client;
use MongoDB\Collection;
use MongoDB\Database;

// Database Creds.
define('DB_USER', "root"); // db user
define('DB_PASSWORD', ""); // db password (mention your db password here)
define('DB_DATABASE', "Contexts"); // database name
define('DB_SERVER', "localhost:27017"); // db server


class NOSQLDB_CONNECT {
		  
    // constructor
    function __construct() {
        // connecting to database
        
    }
 
    // destructor
    function __destruct() {
        // closing db connection
    }
 
    /**
     * Function to connect with database
     */
    function connect() {
       
        $client = new Client("mongodb://".DB_SERVER);
       
        return $client->selectDatabase(DB_DATABASE);
    }
 
    /**
     * Function to close db connection
     */
    function close() {
        // closing db connection
       
    }
 
}
?>