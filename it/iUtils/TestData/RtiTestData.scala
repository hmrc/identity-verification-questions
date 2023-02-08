/*
 * Copyright 2023 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package iUtils.TestData

import play.api.libs.json.Json

trait RtiTestData {
  val rtiResponseJson = Json.parse(
    """
      |{
      |  "queryResult": 0,
      |  "request": {
      |    "nino": "SM081915",
      |    "relatedTaxYear": "14-15",
      |    "requestId": "M1433328147816"
      |  },
      |  "individual": {
      |    "currentNino": "SM081915",
      |    "relatedTaxYear": "14-15",
      |    "employments": {
      |      "employment": [
      |        {
      |          "empRefs": {
      |            "officeNo": "653",
      |            "payeRef": "W1226",
      |            "aoRef": "653PP00002017"
      |          },
      |          "payments": {
      |            "inYear": [
      |              {
      |                "payId": "20425",
      |                "leavingDate": "2012-06-22",
      |                "payFreq": "IO",
      |                "mandatoryMonetaryAmount": [
      |                  {
      |                    "type": "TaxablePayYTD",
      |                    "amount": 0
      |                  },
      |                  {
      |                    "type": "TotalTaxYTD",
      |                    "amount": 10.1
      |                  },
      |                  {
      |                    "type": "TaxablePay",
      |                    "amount": 102.02
      |                  },
      |                  {
      |                    "type": "TaxDeductedOrRefunded",
      |                    "amount": 10
      |                  }
      |                ],
      |                "niLettersAndValues": [
      |                  {
      |                    "niFigure": [
      |                      {
      |                        "type": "EmpeeContribnsYTD",
      |                        "amount": 34.82
      |                      }
      |                    ]
      |                  }
      |                ],
      |                "starter": {
      |                  "startDate": "2011-08-13"
      |                },
      |                "pmtDate": "2014-06-28",
      |                "rcvdDate": "2015-04-06",
      |                "taxYear": "14-15"
      |              }
      |            ]
      |          },
      |          "sequenceNumber": 5
      |        },
      |        {
      |          "empRefs": {
      |            "officeNo": "582",
      |            "payeRef": "TZ99594",
      |            "aoRef": "582PP00002017"
      |          },
      |          "currentPayId": "65553-1808",
      |          "payments": {
      |            "inYear": [
      |              {
      |                "payId": "65553-1808",
      |                "payFreq": "IO",
      |                "mandatoryMonetaryAmount": [
      |                  {
      |                    "type": "TaxablePayYTD",
      |                    "amount": 3000
      |                  },
      |                  {
      |                    "type": "TotalTaxYTD",
      |                    "amount": 11.11
      |                  },
      |                  {
      |                    "type": "TaxablePay",
      |                    "amount": 102.02
      |                  },
      |                  {
      |                    "type": "TaxDeductedOrRefunded",
      |                    "amount": 11
      |                  }
      |                ],
      |                "niLettersAndValues": [
      |                  {
      |                    "niLetter": "L",
      |                    "scon": "S1002306X",
      |                    "niFigure": [
      |                      {
      |                        "type": "EmpeeContribnsInPd",
      |                        "amount": 5
      |                      },
      |                      {
      |                        "type": "EmpeeContribnsYTD",
      |                        "amount": 34.82
      |                      }
      |                    ]
      |                  }
      |                ],
      |                "starter": {
      |                  "startDate": "2013-09-07"
      |                },
      |                "pmtDate": "2014-04-30",
      |                "rcvdDate": "2015-04-06",
      |                "taxYear": "14-15"
      |              }
      |            ]
      |          },
      |          "sequenceNumber": 16
      |        },
      |        {
      |          "empRefs": {
      |            "officeNo": "951",
      |            "payeRef": "YA32406",
      |            "aoRef": "951PP00002017"
      |          },
      |          "currentPayId": "92605301",
      |          "payments": {
      |            "inYear": [
      |              {
      |                "payId": "92605301",
      |                "payFreq": "IO",
      |                "mandatoryMonetaryAmount": [
      |                  {
      |                    "type": "TaxablePayYTD",
      |                    "amount": 1200
      |                  },
      |                  {
      |                    "type": "TaxablePay",
      |                    "amount": 102.02
      |                  },
      |                  {
      |                    "type": "TotalTaxYTD",
      |                    "amount": 0
      |                  },
      |                  {
      |                    "type": "TaxDeductedOrRefunded",
      |                    "amount": 8
      |                  }
      |                ],
      |                "niLettersAndValues": [
      |
      |                ],
      |                "starter": {
      |                  "startDate": "2013-08-17"
      |                },
      |                "pmtDate": "2014-04-30",
      |                "rcvdDate": "2015-04-06",
      |                "taxYear": "14-15"
      |              },
      |              {
      |                "payId": "92605302",
      |                "payFreq": "IO",
      |                "mandatoryMonetaryAmount": [
      |                  {
      |                    "type": "TaxablePayYTD",
      |                    "amount": 1266
      |                  },
      |                  {
      |                    "type": "TotalTaxYTD",
      |                    "amount": 13.13
      |                  },
      |                  {
      |                    "type": "TaxablePay",
      |                    "amount": 108.99
      |                  },
      |                  {
      |                    "type": "TaxDeductedOrRefunded",
      |                    "amount": 10
      |                  }
      |                ],
      |                "niLettersAndValues": [
      |                 {
      |                   "niFigure" : []
      |                 }
      |                ],
      |                "starter": {
      |                  "startDate": "2013-09-17"
      |                },
      |                "pmtDate": "2014-05-30",
      |                "rcvdDate": "2015-05-06",
      |                "taxYear": "14-15"
      |              }
      |            ]
      |          },
      |          "sequenceNumber": 61
      |        }
      |      ]
      |    }
      |  }
      |}
    """.stripMargin)

  val p60ResponseWithEmployerWithoutPaymentsJson = Json.parse(
    """{
      |    "queryResult": 0,
      |    "request": {
      |        "nino": "SM081915",
      |        "relatedTaxYear": "14-15",
      |        "requestId": "M1433328147816"
      |    },
      |    "individual": {
      |        "currentNino": "SM081915",
      |        "relatedTaxYear": "14-15",
      |        "employments": {
      |            "employment": [
      |                {
      |                    "empRefs": {
      |                        "officeNo": "653",
      |                        "payeRef": "W1226",
      |                        "aoRef": "653PP00002017"
      |                    },
      |                    "payments": {
      |                        "inYear": [
      |                            {
      |                                "payId": "20425",
      |                                "leavingDate": "2012-06-22",
      |                                "payFreq": "IO",
      |                                "mandatoryMonetaryAmount": [
      |                                    {
      |                                        "type": "TaxablePayYTD",
      |                                        "amount": 0
      |                                    },
      |                                    {
      |                                        "type": "TotalTaxYTD",
      |                                        "amount": 0
      |                                    },
      |                                    {
      |                                        "type": "TaxablePay",
      |                                        "amount": 102.02
      |                                    },
      |                                    {
      |                                        "type": "TaxDeductedOrRefunded",
      |                                        "amount": 0
      |                                    }
      |                                ],
      |                                "niLettersAndValues": [
      |                                {
      |                                }
      |                                ],
      |                                "starter": {
      |                                    "startDate": "2011-08-13"
      |                                },
      |                                "pmtDate": "2014-06-28",
      |                                "rcvdDate": "2015-04-06",
      |                                "taxYear": "14-15"
      |                            }
      |                        ]
      |                    },
      |                    "sequenceNumber": 5
      |                },
      |                {
      |                    "empRefs": {
      |                        "officeNo": "582",
      |                        "payeRef": "TZ99594",
      |                        "aoRef": "582PP00002017"
      |                    },
      |                    "currentPayId": "65553-1808",
      |                    "payments": {
      |                    },
      |                    "sequenceNumber": 16
      |                }
      |            ]
      |        }
      |    }
      |}""".stripMargin)

  val p60ResponseWithoutEmploymentsJson = Json.parse(
    """{
      |    "queryResult": 0,
      |    "request": {
      |        "nino": "SM081915",
      |        "relatedTaxYear": "14-15",
      |        "requestId": "M1433328147816"
      |    },
      |    "individual": {
      |        "currentNino": "SM081915",
      |        "relatedTaxYear": "14-15",
      |        "employments": {
      |            "employment": [
      |            ]
      |        }
      |    }
      |}""".stripMargin)

  val p60ResponseWithoutEmploymentJson = Json.parse(
    """{
      |    "queryResult": 0,
      |    "request": {
      |        "nino": "SM081915",
      |        "relatedTaxYear": "14-15",
      |        "requestId": "M1433328147816"
      |    },
      |    "individual": {
      |        "currentNino": "SM081915",
      |        "relatedTaxYear": "14-15",
      |        "employments": {
      |        }
      |    }
      |}""".stripMargin)

  val p60ResponseWithoutOptionalFieldsJson = Json.parse(
    """
      |{
      |  "queryResult": 0,
      |  "request": {
      |    "nino": "AA084113",
      |    "relatedTaxYear": "15-16",
      |    "requestId": "M1449317187820"
      |  },
      |  "individual": {
      |    "currentNino": "AA084113",
      |    "relatedTaxYear": "15-16",
      |    "employments": {
      |      "employment": [
      |        {
      |          "empRefs": {
      |            "officeNo": "754",
      |            "payeRef": "SZ00031",
      |            "aoRef": "754PD00002328"
      |          },
      |          "currentPayId": "AA084113",
      |          "payments": {
      |            "inYear": [
      |              {
      |                "payId": "AA084113",
      |                "payFreq": "M1",
      |                "monthNo": "1",
      |                "periodsCovered": 1,
      |                "aggregatedEarnings": true,
      |                "hoursWorked": "30 hrs or more",
      |                "mandatoryMonetaryAmount": [
      |                  {
      |                    "type": "TaxablePayYTD",
      |                    "amount": 1333.33
      |                  },
      |                  {
      |                    "type": "TotalTaxYTD",
      |                    "amount": 90
      |                  },
      |                  {
      |                    "type": "TaxDeductedOrRefunded",
      |                    "amount": 90
      |                  }
      |                ],
      |                "taxCode": {
      |                  "value": "1060L"
      |                },
      |                "starter": {
      |                  "startDate": "2012-10-22",
      |                  "startDec": "C"
      |                },
      |                "pmtDate": "2015-06-24",
      |                "rcvdDate": "2015-0-20",
      |                "pmtConfidence": 4,
      |                "taxYear": "15-16"
      |              },
      |              {
      |                "payId": "AA084113",
      |                "payFreq": "M1",
      |                "monthNo": "3",
      |                "periodsCovered": 1,
      |                "aggregatedEarnings": true,
      |                "hoursWorked": "30 hrs or more",
      |                "mandatoryMonetaryAmount": [
      |                  {
      |                    "type": "TaxablePayYTD",
      |                    "amount": 4000
      |                  },
      |                  {
      |                    "type": "TotalTaxYTD",
      |                    "amount": 270
      |                  },
      |                  {
      |                    "type": "TaxDeductedOrRefunded",
      |                    "amount": 90
      |                  },
      |                  {
      |                    "type": "TaxablePay",
      |                    "amount": 1333.33
      |                  }
      |                ],
      |                "niLettersAndValues": [
      |                  {
      |                    "niLetter": "L",
      |                    "scon": "S1002306X",
      |                    "niFigure": [
      |                      {
      |                        "type": "EmpeeContribnsInPd",
      |                        "amount": 5
      |                      },
      |                      {
      |                        "type": "EmpeeContribnsYTD",
      |                        "amount": 34.82
      |                      }
      |                    ]
      |                  }
      |                ],
      |                "taxCode": {
      |                  "value": "1060L"
      |                },
      |                "pmtDate": "2015-06-25",
      |                "rcvdDate": "2015-06-25",
      |                "pmtConfidence": 0,
      |                "taxYear": "15-16"
      |              }
      |            ]
      |          },
      |          "sequenceNumber": 1
      |        }
      |      ]
      |    }
      |  }
      |}""".stripMargin)

  val p60ResponseWithoutmandatoryMonetaryAmountFieldJson = Json.parse(
    """
      |{
      |  "queryResult": 0,
      |  "request": {
      |    "nino": "AA084113",
      |    "relatedTaxYear": "15-16",
      |    "requestId": "M1449317187820"
      |  },
      |  "individual": {
      |    "currentNino": "AA084113",
      |    "relatedTaxYear": "15-16",
      |    "employments": {
      |      "employment": [
      |        {
      |          "empRefs": {
      |            "officeNo": "754",
      |            "payeRef": "SZ00031",
      |            "aoRef": "754PD00002328"
      |          },
      |          "currentPayId": "AA084113",
      |          "payments": {
      |            "inYear": [
      |              {
      |                "payId": "AA084113",
      |                "payFreq": "M1",
      |                "monthNo": "1",
      |                "periodsCovered": 1,
      |                "aggregatedEarnings": true,
      |                "hoursWorked": "30 hrs or more",
      |                "taxCode": {
      |                  "value": "1060L"
      |                },
      |                "starter": {
      |                  "startDate": "2012-10-22",
      |                  "startDec": "C"
      |                },
      |                "pmtDate": "2015-06-24",
      |                "rcvdDate": "2015-0-20",
      |                "pmtConfidence": 4,
      |                "taxYear": "15-16"
      |              },
      |              {
      |                "payId": "AA084113",
      |                "payFreq": "M1",
      |                "monthNo": "3",
      |                "periodsCovered": 1,
      |                "aggregatedEarnings": true,
      |                "hoursWorked": "30 hrs or more",
      |                "mandatoryMonetaryAmount": [
      |                  {
      |                    "type": "TaxablePayYTD",
      |                    "amount": 4000
      |                  },
      |                  {
      |                    "type": "TotalTaxYTD",
      |                    "amount": 270
      |                  },
      |                  {
      |                    "type": "TaxDeductedOrRefunded",
      |                    "amount": 90
      |                  },
      |                  {
      |                    "type": "TaxablePay",
      |                    "amount": 1333.33
      |                  }
      |                ],
      |                "niLettersAndValues": [
      |                  {
      |                    "niLetter": "L",
      |                    "scon": "S1002306X",
      |                    "niFigure": [
      |                      {
      |                        "type": "EmpeeContribnsInPd",
      |                        "amount": 5
      |                      },
      |                      {
      |                        "type": "EmpeeContribnsYTD",
      |                        "amount": 34.82
      |                      }
      |                    ]
      |                  }
      |                ],
      |                "taxCode": {
      |                  "value": "1060L"
      |                },
      |                "pmtDate": "2015-06-25",
      |                "rcvdDate": "2015-06-25",
      |                "pmtConfidence": 0,
      |                "taxYear": "15-16"
      |              }
      |            ]
      |          },
      |          "sequenceNumber": 1
      |        }
      |      ]
      |    }
      |  }
      |}""".stripMargin)
}
