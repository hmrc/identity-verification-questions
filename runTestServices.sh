#!/usr/bin/env bash

if ([ ${1:-""} == "sm" ] || [ ${1:-""} == "sm2" ]) && ([ ${2} == "stop" ] || [ ${2} == "start" ])
then
    ${1} --${2} IV_TEST_DATA SI_HOD_PROXY BUSINESS_VERIFICATION_STUB DATASTREAM
else
    printf "Parameters not valid!\n1st parameter should be 'sm' or 'sm2'.\nSecond parameter should be 'start' or 'stop'\nE.g. './runTestServices.sh sm2 start'\n"
fi

