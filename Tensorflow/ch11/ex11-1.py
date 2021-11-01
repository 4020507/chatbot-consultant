import requests, json

# 보내기 API 인증키
authorization_key = 'ssPMHOQ0Ru61dk17aoY8' #인증키가 필요, API 설정화면에서 발급한 키를 사용. 그림 11-3 참조
headers = {
    'Content-Type': 'application/json;charset=UTF-8',
    'Authorization': authorization_key,
}

# 사용자 식별값, 보낼 메시지 정의
user_key = 'w472ko' #수신자 번호를 알아야 SMS를 전송을 할 수 있는 것 처럼, 사용자 식별값이 필요하다. user 필드 값을 사용자별로 DB에 저장하여 필요할 때 마다 원하는 사용자에게 메시지 전송 가능
data = {
    "event": "send",
    "user": user_key,
    "textContent": {"text": "hello world :D"}
}

# 보내기 API 호출, 위에서 정의한 headers와 body 데이터를 request 모듈을 이용해 전송
message = json.dumps(data) # JSON 문자열 변경, data에는 무조건 문자열이 들어가야 함
response = requests.post(
    'https://gw.talk.naver.com/chatbot/v1/event',
    headers=headers,
    data=message)

print(response.status_code)
print(response.text)

#https://github.com/navertalk/chatbot-api#%EC%98%A4%EB%A5%98-%EB%AA%85%EC%84%B8 오류 명세