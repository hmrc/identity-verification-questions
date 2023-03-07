
# identity-verification-questions

Backend service to provide question data and answer processing by unique user identifier for all verification services in MDTP.

This service extracts, refactors and replaces the question handling behaviour that is currently part of the https://github.com/hmrc/identity-verification making it available to other services.  This will promote re-use of IV behaviour across the platform and provide consistent verification standards.

Currently, this service only provides questions from the VAT, P60 and self assessment (SA) evidence sources. Other sources are currently being moved from identity-verification backend to here.

## Evidence Sources

* VRN - requires VAT; uses DES API (#1351) to query data for the **totalValueSalesExVAT** and  **totalValuePurchasesExVAT**

* P60 - requires NINO; uses RTI DES API (#1001) to query data for the **current tax year**

* SA - requires UTR or NINO; uses si-hod-proxy API to query user self assessment data.

    * provides SA Payment questions if calling service provides UTR
    * provides SA Pensions questions if calling service provides NINO or SAPayments is not available for user.

See https://confluence.tools.tax.service.gov.uk/pages/viewpage.action?spaceKey=VER&title=IV+Evidence+Sources for a description of evidence sources and the question keys available for each.

More evidence sources will be added in due course.

## Routes

| Method | Path                                             | Description                                           |
|--------|--------------------------------------------------|-------------------------------------------------------|
|  POST  | ```/questions```                                 | Get questions and a correlation id for a set of identifiers  |
|  POST  | ```/answers```                                   | Supply answers for set of previously fetched questions          |

### POST /questions
Include a POST JSON body containing a set of identifiers for the questions such as:
```
    { 
      "vrn": "123456789",
      "nino":"AA000003D",
      "sautr": "1234567890",
      "payeRef": {
        "taxOfficeNumber" : "123",
        "taxOfficeReference" : "4887762099"
      }
    }
```
You must supply *at least one* identifier, and you can only supply one *of each type of identifier*.
Different identifier types support different **evidence sources** for question data.  Currently the **only** supported identifiers are:

* **vrn** - Must be a valid VAT Registration Number according to https://github.com/hmrc/domain/blob/main/src/main/scala/uk/gov/hmrc/domain/Vrn.scala

* **nino** - Must be a valid NINO (with suffix) according to https://github.com/hmrc/domain/blob/main/src/main/scala/uk/gov/hmrc/domain/Nino.scala

* **sautr** - Must be a valid SaUtr according to https://github.com/hmrc/domain/blob/main/src/main/scala/uk/gov/hmrc/domain/SaUtr.scala

* **payeRef** - Must be a valid payeRef according to https://github.com/hmrc/domain/blob/main/src/main/scala/uk/gov/hmrc/domain/EmpRef.scala

## Authorised Callers

In order to use this service, you must be **authorised** by the Verification team to integrate with it - we have an **allow list** of clients (services) which can call our endpoints; currently limited to "identity-verification" and "lost-credentials".  Please contact [#team-verification](https://hmrcdigital.slack.com/archives/C0L6KFBQQ) in order to request access to this service.

#### Response

| Status | Description                                                                                                                                             |
|--------|---------------------------------------------------------------------------------------------------------------------------------------------------------|
| 200    | Returns a correlation id, along with a (possibly empty) set of questions for the identifiers given|
| 403    | You are not authorized to use the question repository - see above |
|

Example response, showing 2 possible questions that can be asked (both from P60 evidence source):

```
{
  "correlationId": "d705f45e-14fd-4b99-ae7c-b1d4446d659b",
  "questions": [
    {
      "questionKey": "rti-p60-payment-for-year",
      "info": {
        "currentTaxYear": "2021/22",
        "previousTaxYear": "2020/21"
      }
    },
    {
      "questionKey": "rti-p60-employee-ni-contributions",
      "info": {
        "currentTaxYear": "2021/22",
        "previousTaxYear": "2020/21"
      }
    }
  ]
}
```

The question keys returned will depend on the available data for the submitted identifier(s).  The question keys indicate that there is data in the evidence source which supports the question, and answers can be collected from the user for these.

If the evidence sources found no data for the identifier(s), then an empty list of questions will be returned.

Possible question keys for each evidence source are documented here:

https://confluence.tools.tax.service.gov.uk/pages/viewpage.action?spaceKey=VER&title=IV+Evidence+Sources

Each **questionKey** represents a particular **data point** from an evidence source which can be posed as a question to the user.

It will be different depending on the question key.

| Question Key                            | Description              |
|-----------------------------------------|--------------------------|
|  sa-income-from-pensions                | Income from pensions     |
|  sa-payment-details                     | Payment details          |
|  rti-payslip-income-tax                 | Tax deducted             |
|  rti-payslip-national-insurance         | NI amount                |
|  paye-date-of-payment                   | Payment date             |
|  paye-payment-amount                    | Payment amount           |
|  ita-bankaccount                        | Bank account             |
|  tc-amount                              | Tax credit amount        |
|  rti-p60-payment-for-year               | Total for year           |
|  rti-p60-employee-ni-contributions      | Employee's contribution  |
|  rti-p60-statutory-adoption-pay         | Adoption Pay             |
|  rti-p60-earnings-above-pt              | Earnings above the PT    |
|  rti-p60-statutory-maternity-pay        | Maternity Pay            |
|  rti-p60-postgraduate-loan-deductions   | Postgraduate Loans       |
|  rti-p60-statutory-shared-parental-pay  | Shared Parental Pay      |
|  rti-p60-student-loan-deductions        | Student Loans            |
|  value-of-sales-amount                  | Total Value Sales ExVAT  |
|  value-of-purchases-amount              | Total Value Purchases ExVAT |


For more description of the info object data for each question, see confluence page or scaladoc in:

https://confluence.tools.tax.service.gov.uk/pages/viewpage.action?pageId=425951833

https://github.com/hmrc/identity-verification-questions/blob/main/app/uk/gov/hmrc/identityverificationquestions/models/QuestionKey.scala

Correct answers for each question are usually stored internally (for later checking), but we **never** provide the answers to the client at this stage.  Note that some evidence options are in fact queried during the answer phase of the calls.  Questions should be posed to the user, and then entered answers submitted for checking using the following endpoint:

### POST /answers
Send a json body containing the previous correlation id and identifier selection as given above, along with answer data for each question from the user, such as:
```
    {
      "correlationId": "66d89bcf-847e-446e-b0e9-348591d118d3",
      "selection": {
        "nino":"AA000000A"
      },
      "answers":[
        {
          "questionKey": "rti-p60-payment-for-year",
          "answer": "100.00"
        },
        {
          "questionKey": "rti-p60-employee-ni-contributions",
          "answer": "250.00"
        }
      ]
}
```
```
    {
      "correlationId": "66d89bcf-847e-446e-b0e9-348591d118d3",
      "selection": {
        "vrn": "123456789"
      },
      "answers":[
        {
          "questionKey": "value-of-sales-amount",
          "answer": "5000.00"
        },
        {
          "questionKey": "value-of-purchases-amount",
          "answer": "2500.00"
        }
      ]
}
```
For answers calls from IV the body should also contains some iv journey details for auditing purpose, 
```
    {
      "correlationId": "66d89bcf-847e-446e-b0e9-348591d118d3",
      "selection": {
        "nino":"AA000000A"
      },
      "answers":[
        {
          "questionKey": "rti-p60-payment-for-year",
          "answer": "100.00"
        },
        {
          "questionKey": "rti-p60-employee-ni-contributions",
          "answer": "250.00"
        }
      ],
      "ivJourney": {
        "journeyId":"journeyId"，
        "journeyType":"journeyType"，
        "authProviderId":"authProviderId"，
        "origin":"origin"
      }
}
```
PS. if your service needs some special requirements like iv please connect team verification.

The structure of the answer data will depend on the **questionKey** being checked.

For now,
* for **all** P60/ SA Pension evidence source questions, a *single string* answer is expected.
* for **all** SA Payments evidence source questions, a json in *single string* answer is expected. Example:

```
{
          "amount": 100,
          "paymentDate": "2020-06-01"
}       
```
For details of the Answer *formats* for each question see:

https://github.com/hmrc/identity-verification-questions/blob/main/app/uk/gov/hmrc/identityverificationquestions/models/Answer.scala

TODO add more examples for different evidence sources.

#### Response

| Status | Description                                           |
|--------|-------------------------------------------------------|
| 200    | The outcome of the answer check, indicating a correct or incorrect response|
| 403    | You are not authorized to use the question repository - see above |
| 404    | The supplied correlation id and selection did not match any known questions|

The outcome of each question answered will be provided in the response, for example:

```
[
  {
    "questionKey": "rti-p60-payment-for-year",
    "score": "incorrect"
  },
  {
    "questionKey": "rti-p60-employee-ni-contributions",
    "score": "correct"
  }
]
```

The possible values for "score" are:

* "correct" - the supplied answer was correct
* "incorrect" - the answer was incorrect (or missing)
* "unknown" - no matching question/answer data found for correlation id
* "error" - there was an error from an evidence source while checking the answer

Answers which were expected but not supplied in the query will be considered "incorrect".
It is up to the calling service to decide which answers they would like to know the score of.

## How to run the tests

```sbt clean test``` and  ```sbt clean it:test```

Note: acceptance testing is done as part of the main IV suite with question repository enabled from IV backend, see https://github.com/hmrc/identity-verification-ui-tests

## How to run the service locally

This service depends on the following collaborating services at runtime:

* datastream (for Splunk auditing)
* platform-analytics (for GA events)
* iv_test_data (during IT tests and in stubbed or local environments)
* si_hod_proxy (for SA questions)
* business_verification_stub (during IT tests and in stubbed or local environments)
* des.ws.hmrc.gov.uk (in production, for P60 data)

Locally, make sure you have https://github.com/hmrc/iv-test-data running and then do:

```sbt run```

The service should start up on port 10101
