package com.zhangs_tech.runtomo.server.quest

class QuestNotFoundException : RuntimeException("Quest not found")

class QuestNotCompleteException : RuntimeException("Quest is not complete")

class QuestAlreadyClaimedException : RuntimeException("Quest is already claimed today")
