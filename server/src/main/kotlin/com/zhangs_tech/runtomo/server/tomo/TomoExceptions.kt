package com.zhangs_tech.runtomo.server.tomo

class InvalidNicknameException : RuntimeException("Nickname must be 1 to 16 characters")

class NotAStarterException : RuntimeException("Species is not a starter")

class TomoNotFoundException : RuntimeException("Tomo not found")

class StarterAlreadyChosenException : RuntimeException("Player already owns a Tomo")
