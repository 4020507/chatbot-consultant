import pickle

with open("chatbot_dict.bin","rb") as fr:
    data = pickle.load(fr)
from utils.Preprocess import Preprocess

print(data)
