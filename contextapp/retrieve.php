<?php
$response=array();
$response["success"]=-1;
$response["errors"]="";
$response["results"]=array();
if (isset($_POST["lat"]) && isset($_POST["lng"]) && isset($_POST["radius"])){
    require_once __DIR__ . './nosql_connect.php';
    require_once __DIR__ . './distance.php';
   
    // Class object declared in nosql_connect
    $db= new NOSQLDB_CONNECT();
    
    // call connect method which will return object to DB defined in nosql_connect
    $db_c= $db->connect();
    
    // now select collection from DB
    $collection= $db_c->atms;
    // get all documents from collection
    $data_iterator= $collection->find();
    // convert data to document array for easy travesal
    $it= iterator_to_array($data_iterator);
//    var_dump($it);
    // Iterate over each document in array
    foreach ($it as $doc) {
        $dis=distance($doc["lat"],$doc["lng"],$_POST["lat"],$_POST["lng"],"METER");
        if ($dis>$_POST["radius"])
            continue;
        // build one result for JSON
        $one_result=array();
//        $one_result["id"]=$doc["_id"];
        $one_result["lat"]=$doc["lat"];
        $one_result["lng"]=$doc["lng"];
        $one_result["place"]=$doc["place"];
        
        $one_result["bank_name"]=$doc["bank_name"];
        $one_result["dis"]=$dis;
        // push the one result in array
        array_push($response["results"],$one_result);
   //var_dump($doc);
    }
    // success will notify the app about request
    $response["success"]=1;
    echo json_encode($response);
}
else{
    $response["errors"]="error";
    $response["success"]=-2;
    echo json_encode($response);
//    var_dump($_POST);
}
?>