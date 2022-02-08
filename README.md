
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
#### Response

| Status | Description                                                                                                                                             |
|--------|---------------------------------------------------------------------------------------------------------------------------------------------------------|
| 200    | QuestionResponse(correlationId: CorrelationId, questions: Seq[Question], questionTextEn: Map[String,String], questionTextCy: Option[Map[String,String]])|

QuestionResponse(ce69ffd8-c656-41c5-acd3-e80a24e98de2,List(Question(PaymentToDate,List(),Map(currentTaxYear -> 2020/21)), Question(EmployeeNIContributions,List(),Map(currentTaxYear -> 2020/21))),Map(PaymentToDate.retry.one-year -> Enter the amount of total pay for the year {0}, as shown on your P60. Enter the exact amount, including pence. For example 22643.51, EmployeeNIContributions.match.error -> etc.))

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
#### Response

| Status | Description                                           |
|--------|-------------------------------------------------------|
| 200    | QuestionResult(questionKey: QuestionKey, score: Score)|

Vector(QuestionResult(PaymentToDate,correct))