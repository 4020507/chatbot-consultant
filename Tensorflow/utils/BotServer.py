import socket

class BotServer:
    def __init__(self, srv_port, listen_num): #srv_port, 포트 번호, listen_num: 연결을 허락 할 클라이언트 수
        self.port = srv_port
        self.listen = listen_num
        self.mySock = None

    # sock 생성, socket을 생성한 뒤 self.port로 self.listen 수 만큼 클라이언트 연결을 수락
    def create_sock(self):
        self.mySock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        self.mySock.bind(("0.0.0.0", int(self.port)))
        self.mySock.listen(int(self.listen))
        return self.mySock

    # client 대기, 클라이언트가 연결을 요청하는 즉시 accept()함수가 클라이언트와 통신할 수 있도록 클라이언트용 소켓 객체 반환
    #반환 값은 conn, address, conn: 연결된 챗봇 클라이언트와 데이터를 송수신할 수 있는 클라이언트 소켓
    #address: 연결된 챗봇 클라이언트 소켓의 바인드된 주소
    def ready_for_client(self):
        return self.mySock.accept()

    # sock 반환
    def get_sock(self):
        return self.mySock