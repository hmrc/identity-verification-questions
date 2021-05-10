
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
  