#ifndef COMMON_H
#define COMMON_H

#define MAX_ELEMENTS 100
#define PORT 3338

typedef struct{
    int choice;
    int n;
    int y[MAX_ELEMENTS];
    float a;
} Request;

typedef struct{
    int choice;
    int n;
    float mean;
    int min;
    int max;
    float result[MAX_ELEMENTS];
    int status;
} Response;

#endif