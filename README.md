# Antalya Bus Telegram Bot

## Description

This bot can be used for tracking buses in Antalya. 

It is not a replacement for AntalyaKart app. However, it might be more useful in some particular cases:

- If you are near the stop and know which bus you need to jump on, you can just type the stop ID and see timings for all upcoming buses.
- If you want to track buses periodically, you can put all buses (or some particular bus) on track. This might be convenient if you are at the stop and want to constantly receive current status for buses. Or if you are at home waiting for some bus to come to the stop near you, and willing to go out just before bus arrival, chances are lower with this bot that you'll miss the bus. 
- If you want to track some bus that is passing through the stop rarely, you can put it on track. Typically, buses are seen ~30 min prior to coming. But if you put bus on track, you will see where the bus is even if it's anywhere on the route. The difference between the bus and the stop will be shown in stop diffs rather than in time diff for this case, since it is hard to calculate number of minutes properly. Note that due to limitations of KentKart API, this works only for tracking one particular bus. 

> This bot can be potentially expanded to other cities supported by KentKart. List of cities supported by KentKart can be found here: [KentKart Cities](https://m.kentkart.com/cities).

## Useful notes

- Tracking interval can be customized: 1, 2 or 3 minutes.
- Bot supports 3 languages: English, Russian and Turkish. Note that since my Turkish is poor, there might be multiple translation issues.
- It is possible to get 3 nearest stops to the user, ordered by distance.
- It is possible to add up to 6 stops to the favorite list and access it from the menu. This is like a shortcut, not to enter bus stop number each time.
- Sometimes bus is not dropped off the bus station. Moreover, it may hung up for some time in this state. Such buses are represented as strikethrough. Note that this feature works only for Sarısu bus station.
- Other details on usage of the bot can be found in /help section.

## Implementation details

- This is a Java 17 application developed via Spring Boot 3. 
- This bot uses KentKart API under the hood (same API that AntalyaKart mobile app is using).
- User information (including custom tracking interval, favorite stops, etc.) is stored via writing/reading to a file in JSON format. Writing is scheduled per day.
- This bot collects statistics (number of unique users and number of total requests) per day, writing it to the file.