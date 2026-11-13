package com.zhangs_tech.runtomo.server.food

class FoodNotFoundException : RuntimeException("Food not found")

class NoFoodLeftException : RuntimeException("The Player has none of this food")

/** UC-18 6a: the Tomo is full. The Player keeps the food. */
class FeedingLimitException : RuntimeException("The Tomo is full for today")
