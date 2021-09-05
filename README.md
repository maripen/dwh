# Simple Data Warehouse (extract, transform, load, query)
Simple backend application that exposes data - extracted from a csv file - via an API.
The system will automatically create the schema based on the input csv data types, 
each uploaded csv is available in a separate collection by its filename. 

## Technologies
- [Spring Boot](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/)
- [MongoDB](https://docs.mongodb.com/manual/introduction/)
- [Lombok](https://projectlombok.org/)
- [Docker](https://www.docker.com/)

## API Documentation
The API documentation can be checked at: `localhost:8080/swagger-ui.html`

## Running locally
Execute:
```
./gradlew clean bootjar
docker-compose build
docker-compose up -d
```

## CSV file data import
The system can import a csv file from a specific URL:
```
curl -X POST localhost:8080/import/csv -H "Content-Type: application/json" -d '{
   "url": "http://adverity-challenge.s3-website-eu-west-1.amazonaws.com/PIxSyyrIKFORrCXfMYqZBI.csv"
}'
```

### Requested report use cases
#### Total _Clicks_ for a given _Datasource_ for a given _Date_ range
```
curl -X POST localhost:8080/query/ -H "Content-Type: application/json" -d  '{
    "filter": {
        "Datasource" : { "is": "Twitter Ads" },
        "Daily" : {
            "gte" : "11/12/19",
            "lte" : "12/24/19"
        }
    },
    "groupBy" : [
      "Datasource"
    ],
    "aggregate" : {
        "Clicks" : "sum"
    },
    "collectionName" : "PIxSyyrIKFORrCXfMYqZBI"
}'
```
#### _Click-Through Rate (CTR)_ per _Datasource_ and _Campaign_
```
curl -X POST localhost:8080/query/ -H "Content-Type: application/json" -d  '{
    "groupBy" : [
        "Datasource", "Campaign"
    ],
    "calculatedField" : {
        "CTR" : "Clicks / Impressions"
    },
    "aggregate" : {
        "Impressions" : "sum",
        "Clicks": "sum"
    },
    "collectionName" : "PIxSyyrIKFORrCXfMYqZBI"
}'
```

#### _Impressions_ over time (daily)
```
curl -X POST localhost:8080/query/ -H "Content-Type: application/json" -d  '{
    "groupBy" : [
        "Daily"
    ],
    "aggregate" : {
        "Impressions" : "sum"
    },
    "collectionName" : "PIxSyyrIKFORrCXfMYqZBI"
}'
```
