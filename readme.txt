Para compilar o cliente e o servidor:
./compile.sh

Para correr o servidor deve colocar-se na pasta PhotoShareServer e correr o seguinte comando:
java -Djava.security.manager -Djava.security.policy==src/server.policy -cp out/ PhotoShareServer 23232

Para correr o cliente deve colocar-se na pasta PhotoShareCliente e correr um dos seguintes comandos:
    - Criar novo utilizador:
    java -Djava.security.manager -Djava.security.policy==src/client.policy -cp out/ PhotoShare <user> <password> <ip>:23232

    - Adicionar uma foto: (as fotos devem estar dentro da pasta PhotoShareClient)
    java -Djava.security.manager -Djava.security.policy==src/client.policy -cp out/ PhotoShare <user> <password> <ip>:23232 -a <photo1> [<photo2> <photo3> ...]

    - Listar fotos de um utilizador:
    java -Djava.security.manager -Djava.security.policy==src/client.policy -cp out/ PhotoShare <user> <password> <ip>:23232 -l <userId>

    - Comentarios e numero de likes/dislikes de uma foto:
    java -Djava.security.manager -Djava.security.policy==src/client.policy -cp out/ PhotoShare <user> <password> <ip>:23232 -i <userId> <photo>

    - Download das fotos de um utilizador:
    java -Djava.security.manager -Djava.security.policy==src/client.policy -cp out/ PhotoShare <user> <password> <ip>:23232 -g <userId>

    - Adicionar um comentario a uma foto:
    java -Djava.security.manager -Djava.security.policy==src/client.policy -cp out/ PhotoShare <user> <password> <ip>:23232 -c <comentario> <userId> <photo>

    - Adicionar um like a uma foto:
    java -Djava.security.manager -Djava.security.policy==src/client.policy -cp out/ PhotoShare <user> <password> <ip>:23232 -L <userId> <photo>

    - Adicionar um dislike a uma foto:
    java -Djava.security.manager -Djava.security.policy==src/client.policy -cp out/ PhotoShare <user> <password> <ip>:23232 -D <userId> <photo>

    - Adicionar seguidor(es):
    java -Djava.security.manager -Djava.security.policy==src/client.policy -cp out/ PhotoShare <user> <password> <ip>:23232 -f <userId1> [<userId2> <userId3> ...]

    - Remover seguidor(es):
    java -Djava.security.manager -Djava.security.policy==src/client.policy -cp out/ PhotoShare <user> <password> <ip>:23232 -r <userId1> [<userId2> <userId3> ...]
