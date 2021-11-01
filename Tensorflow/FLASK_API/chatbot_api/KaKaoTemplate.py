class KakaoTemplate:
    def __init__(self):
        self.version = "2.0"

    def simpleTextComponent(self,text): #텍스트만 출력하기
        return {
            "simpleText": {"text": text}
        }

    def simpleImageComponent(self,imageUrl,altText): #이미지만 출력하기
        return{
            "simpleImage": {"imageUrl": imageUrl, "altText": altText}
        }

    def send_response(self,bot_resp): #사용자에게 응답 스킬 전송하기
        responseBody = {
            "version": self.version,
            "template":{
                "outputs": []
            }
        }

        #이미지 답변이 있는 경우 이미지 답변이 텍스트 보다 먼저 출력하도록 함

        if bot_resp['AnswerImageUrl'] is not None:
            responseBody['template']['outputs'].append(
                self.simpleImageComponent(bot_resp['AnswerImageUrl'],'')
            )

        #텍스트 답변이 있다면
        if bot_resp['Answer'] is not None:
            responseBody['template']['outputs'].append(
                self.simpleTextComponent(bot_resp['Answer'])
            )

        return responseBody