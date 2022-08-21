# Reservation Service 

This service will store reservation information.

## Service Base Url

``
http://localhost:8080
``

## Service Endpoints

### Saving information
```
Post http://localhost:8080/api/reservation/reservation
This will validate and save reservation information

The format of the dates is year, month and day or uuuu-MM-dd for both dates.

It accepts a json body that fit into a format as such
{
    "fullName": "bob",
    "email": "bob@gmail.com",
    "arrivalDate": "2022-08-20",
    "departureDate": "2022-08-23"
}

If there is any issues with request will it return somethings like

{
    "status": "ERROR
    "errors": {
        "arrivalDate" : "already existing reservation for date"    
    }
}
with possible values
"fullName": "full name is missing",
"email": "email is missing",
"arrivalDate": "arrival date is missing",
"departureDate": "departure date is missing"
"departureDate": "departure date is before arrival date",
"arrivalDate": "arrival date must be more than one day in the future",
"arrivalDate" "arrival date must not be more than one month in the future",
"arrivalDate": "already existing reservation for date",
"departureDate": "total days of reservation is too long"

```

### Updating Reservation information

```
Put http://localhost:8080/api/reservation/reservation
This will update reservation information for the given id

The format of the dates is year, month and day or uuuu-MM-dd for both dates.

It accepts a json body that fit into a format as such
{
    "id" : 123,
    "arrivalDate": "2022-08-20",
    "departureDate": "2022-08-23"
}

If the update is successful the endpoint will return back in object that looks like
{
    "id":123,
    "status": "SUCCESS"
}
If the request can not be completed like this where the field name and the reason for the error.
{
    "status": "ERROR
    "errors": {
        "arrivalDate" : "already existing reservation for date"    
    }
}

The types of errors will have the same type have values as the same except for the 
"id": "id given does not match existing reservations ids"

```

### Check Availability 

```
Get http://localhost:8080/api/reservation/reservations/2022-08-01?endDate=2022-08-31

This will get the availability for the given date range.
The end date is optional and the default end date is one month after the start date.

The format of the dates is year, month and day or uuuu-MM-dd for both dates.

This will return an json object 
{
    "reservationDates": [
        {
            "startDate":"2022-08-01",
            "endDate": "2022-08-15"
        },
        {
            "startDate":"2022-08-21",
            "endDate": "2022-08-30"
        }
    ]
}

```


### Delete Reservation

```
Delete http://localhost:8080/api/reservation/reservation/123

This will deactivate the reservation for the given id. 
If the id does not exist it will return a false from the service 
to signal that it could not be deleted

```

## Python script

In the resource folder this is a test.py that exist to test the service but 
only the save and check availability.

Please use python 3 also have requests installed

## Future ideas
1. add file save for h2 as it is only in-memory
2. add client and move the existing object to the client
   1. after I would move the local date serializer and deserializer to the client
3. add more the python script to handle and use inputs