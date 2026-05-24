CC = gcc
CFLAGS = -Wall -I/usr/include/tirpc
LIBS = -ltirpc

all: rpc_server socket_server socket_client

ask1.h ask1_clnt.c ask1_svc.c ask1_xdr.c ask1_client.c ask1_server.c: ask1.x
	rpcgen ask1.x

rpc_server: ask1_server.c ask1_svc.c ask1_xdr.c ask1.h
	$(CC) ask1_server.c ask1_svc.c ask1_xdr.c -o rpc_server $(CFLAGS) $(LIBS)

socket_server: server_sock.c ask1_clnt.c ask1_xdr.c ask1.h common.h
	$(CC) server_sock.c ask1_clnt.c ask1_xdr.c -o socket_server $(CFLAGS) $(LIBS)

socket_client: client_sock.c common.h
	$(CC) client_sock.c -o socket_client