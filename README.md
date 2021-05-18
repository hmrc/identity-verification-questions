
# Question-repository

Service to provide verification questions for all services that require them, filtered by evidence sources appropriate to origin and unique identifier.



| Method | Path                                             | Description                                           |
|--------|--------------------------------------------------|-------------------------------------------------------|
|  POST  | ```/questions```                                 | Get Questions for a specific service utilising an id  |
|  POST  | ```/answers```                                   | Give answers for specific questions with ids          |


### POST /questions
Send a json body containing the question origin, a sequence of identifies for the questions, max number of questions (Optional) and min number of questions (Optional) such as: 
```
    { 
      "origin": "lost-credentials",
      "identifiers": [{"nino":"AA000000A"}],
      "max": "5",
      "min": "3"
    }
```
  
### POST /answers
Send a json body containing the question origin, a sequence of identifies for the questions and answers, such as: 
```
    {
        "correlationId": "66d89bcf-847e-446e-b0e9-348591d118d3",
        "origin":"origin",
        "identifiers":[{"dob":"1984-10-10"}],
        "answers":[{"questionKey":"DVLAQuestion", 
                    "answer":{"drivingLicenceNumber": "drivingLicenceNumber",
                              "surname": "surname",
                              "validFrom": "2020-04-23",
                              "validTo": "2020-04-23",
                              "issueNumber": "issueNumber"}
                  }]
    }
```