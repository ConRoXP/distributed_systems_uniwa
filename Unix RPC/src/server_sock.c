#include <sys/types.h>
#include <sys/socket.h>
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <arpa/inet.h>
#include <sys/wait.h>
#include <rpc/rpc.h>
#include <string.h>

#include "ask1.h"
#include "common.h"

void manage_client(int);

int main(){
    int server_sock, client_sock;
    struct sockaddr_in server_addr, client_addr;
    socklen_t client_len;
    pid_t pid;

    //Socket init
    server_sock= socket(AF_INET, SOCK_STREAM, 0);
    if(server_sock< 0){
        perror("Server socket error");
        exit(-1);
    }

    memset(&server_addr, 0, sizeof(server_addr));
    server_addr.sin_family= AF_INET;
    server_addr.sin_addr.s_addr= INADDR_ANY;
    server_addr.sin_port= htons(PORT);

    if(bind(server_sock, (struct sockaddr *)&server_addr, sizeof(server_addr))< 0){
        perror("Server bind error");
        exit(-1);
    }

    if(listen(server_sock, 5)< 0){
        perror("Server listen error");
        exit(-1);
    }

    while(1){
        client_len= sizeof(client_addr);
        client_sock= accept(server_sock, (struct sockaddr *)&client_addr, &client_len);
        if(client_sock< 0){
            perror("Server accept error");
            continue;
        }

        //Forking to handle multiple clients
        pid= fork();
        if(pid< 0){
            perror("Server fork error");
            close(client_sock);
            continue;
        }

        if(pid== 0){
            close(server_sock);
            manage_client(client_sock);
        }
        else{
            close(client_sock);
            //Zombie procc prevention
            while(waitpid(-1, NULL, WNOHANG)> 0);
        }
    }

    close(server_sock);
    return 0;
}

void manage_client(int client_sock){
    Request req;
    Response resp;
    CLIENT *clnt;
    ssize_t nread;

    //Define client & connection for RPC
    clnt= clnt_create("localhost", CALC_PROG, CALC_VERS, "tcp");
    if(clnt== NULL){
        clnt_pcreateerror("server localhost error");
        close(client_sock);
        exit(-1);
    }

    //Read client data
    while((nread= read(client_sock, &req, sizeof(req)))> 0){
        //Response struct init
        memset(&resp, 0, sizeof(resp));
        resp.choice= req.choice;
        resp.n= req.n;
        resp.status= 0;

        if(req.n<= 0 || req.n> MAX_ELEMENTS){
            resp.status= -1;
            //Send error status to client and exit
            if(write(client_sock, &resp, sizeof(resp)) < 0){
                perror("First server write error");
                break;
            }
            continue;
        }

        if(req.choice== 1){
            int_vector vec;
            float *res;

            vec.data.data_len= req.n;
            vec.data.data_val= req.y;

            res= mean_1(&vec, clnt);
            if(res== NULL){
                clnt_perror(clnt, "mean RPC error");
                resp.status= -1;
            }
            else resp.mean= *res;
        }
        else if(req.choice== 2){
            int_vector vec;
            minmax_result *res;

            vec.data.data_len= req.n;
            vec.data.data_val= req.y;

            res= minmax_1(&vec, clnt);
            if(res== NULL){
                clnt_perror(clnt, "minmax RPC error");
                resp.status= -1;
            }
            else{
                resp.min= res->min;
                resp.max= res->max;
            }
        }
        else if(req.choice== 3){
            scalar_vector_input input;
            float_vector *res;

            input.a= req.a;
            input.vec.data.data_len= req.n;
            input.vec.data.data_val= req.y;

            res= multiply_1(&input, clnt);
            if(res== NULL){
                clnt_perror(clnt, "multiply RPC error");
                resp.status= -1;
            }
            else{
                resp.n= res->data.data_len;
                for(int i= 0; i< resp.n; i++){
                    resp.result[i]= res->data.data_val[i];
                }
            }
        }
        else resp.status= -1;
        //Send results to client
        if(write(client_sock, &resp, sizeof(resp)) < 0){
            perror("Server final write error");
            break;
        }
    }
    clnt_destroy(clnt);
    close(client_sock);
    exit(0);
}