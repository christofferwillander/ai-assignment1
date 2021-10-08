# ai-assignment1 (Kalaha)
Assignment 1 in course Applied Artificial Intelligence (DV2557). AI implementation for Kalaha player client.

## Authors
Oliver Byström and Christoffer Willander

## Functionality
The solution consists of a *search algorithm* and a *opening move selector*.

### AI Search algorithm
 - Searches for optimal solution using iterative deepening search.
 - Has a 5 second limit before the optimal solution that was found is selected.

### Opening move selector
In order to choose the optimal opening move it uses a opening handbook. The handbook uses a csv-format to symbolize the win/lose relationship for each ambo.

Example:
```
amboNumber,numberOfWins,numberOfLossesAndTies
```
```
1,1,2
2,1,2
3,1,2
4,1,2
5,29,5
6,1,1
```

The ambo with the best win/lose ratio is selected as the opening move.
