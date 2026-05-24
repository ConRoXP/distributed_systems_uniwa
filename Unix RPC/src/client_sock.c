#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <arpa/inet.h>
#include <sys/socket.h>

#include "common.h"

int main(){
    int sock;
    struct sockaddr_in server_addr;
    Request req;
    Response resp;

    //Socket init
    sock= socket(AF_INET, SOCK_STREAM, 0);
    if(sock< 0){
        perror("Client socket error");
        exit(-1);
    }

    memset(&server_addr, 0, sizeof(server_addr));
    server_addr.sin_family= AF_INET;
    server_addr.sin_port= htons(PORT);
    server_addr.sin_addr.s_addr= inet_addr("127.0.0.1");

    if(connect(sock, (struct sockaddr *)&server_addr, sizeof(server_addr))< 0){
        perror("Client connect error");
        close(sock);
        exit(-1);
    }

    while(1){
        //Request & Response structs init
        memset(&req, 0, sizeof(req));
        memset(&resp, 0, sizeof(resp));

        printf("\nΔιαθέσιμες ενέργειες:\n");
        printf("1. Μέση τιμή διανύσματος Υ\n");
        printf("2. Μέγιστη και ελάχιστη τιμή του Υ\n");
        printf("3. Γινόμενο α*Y\n");
        printf("0. Εξοδος\n");
        printf("Επιλογή: ");
        fflush(stdout);
        scanf("%d", &req.choice);
        if(req.choice== 0)
            break;
        if(req.choice < 0 || req.choice > 3){
            printf("Μη έγκυρη επιλογή.\n");
            continue;
        }

        printf("Εισάγεται μέγεθος n του διανύσματος (1-%d): ", MAX_ELEMENTS);
        scanf("%d", &req.n);
        if(req.n <= 0 || req.n >= MAX_ELEMENTS){
            printf("Μη έγκυρο μήκος.\n");
            continue;
        }

        printf("Εισάγετε %d ακέραια στοιχεία του Υ:\n", req.n);
        for(int i= 0; i< req.n; i++){
            printf("Y[%d]= ", i);
            fflush(stdout);
            scanf("%d", &req.y[i]);
        }

        if(req.choice== 3){
            printf("Εισάγετε πραγματικό αριθμό α: ");
            fflush(stdout);
            scanf("%f", &req.a);
        }

        //Send
        if(write(sock, &req, sizeof(req))< 0){
            perror("Client write error");
            break;
        }
        //Receive
        if(read(sock, &resp, sizeof(resp))<= 0){
            perror("Client read error");
            break;
        }

        //Response.status field check
        if(resp.status!= 0){
            printf("Server returned error.\n");
            continue;
        }

        if(resp.choice== 1){
            printf("Μέση τιμή Y= %.2f\n", resp.mean);
        }
        else if(resp.choice== 2){
            printf("Min= %d, Max= %d\n", resp.min, resp.max);
        }
        else if(resp.choice== 3){
            printf("α*Υ= ");
            for(int i= 0; i< resp.n; i++){
                if(i< resp.n-1)
                    printf("%.2f, ", resp.result[i]);
                else
                    printf("%.2f", resp.result[i]);
            }
            printf("\n");
        }
    }

    close(sock);
    return 0;
}