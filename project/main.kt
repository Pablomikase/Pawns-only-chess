import kotlin.math.abs

private var playerOneNickname = ""
private var playerTwoNickname = ""
private val userInputRegex = "[a-h][1-8][a-h][1-8]".toRegex()


var whitePawnHotSpot = ""
var blackPawnHotSpot = ""

enum class StatusGame {
    PLAYER_ONE_ACTIVE, PLAYER_TWO_ACTIVE
}

enum class Winner {
    PLAYER_ONE, PLAYER_TWO, NONE, STALEMATE
}

enum class Moves {
    NO_WHITE_PAWN, NO_BLACK_PAWN, INVALID_INPUT, VALID_MOVE
}

fun main() {

    //Board definition
    val board = definStartingBoard()

    //Print Title
    println("Pawns-Only Chess")

    //ask player's one nickname
    println("First Player's name:")
    playerOneNickname = readln()

    //askplayer's two nickname
    println("Second Player's name:")
    playerTwoNickname = readln()

    printBoard(board)

    startGame(board)

}

fun startGame(board: MutableList<MutableList<Char>>) {

    var statusGame = StatusGame.PLAYER_ONE_ACTIVE

    while (true) {
        when (statusGame) {
            StatusGame.PLAYER_ONE_ACTIVE -> {
                val playerMove = askPlayerMove(statusGame)
                if (playerMove == "exit") return
                if (executeMoveForPLayer(statusGame, board, playerMove))
                    statusGame = StatusGame.PLAYER_TWO_ACTIVE
                val result = checkIfThereIsAWinner(board)
                if (result == Winner.PLAYER_ONE) {
                    println(
                            "White Wins!\n" +
                                    "Bye!"
                    )
                    return
                }
                if (result == Winner.STALEMATE) return
                continue
            }
            StatusGame.PLAYER_TWO_ACTIVE -> {
                val playerMove = askPlayerMove(statusGame)
                if (playerMove == "exit") return
                if (executeMoveForPLayer(statusGame, board, playerMove))
                    statusGame = StatusGame.PLAYER_ONE_ACTIVE
                val result = checkIfThereIsAWinner(board)
                if (result == Winner.PLAYER_TWO) {
                    println(
                            "Black Wins!\n" +
                                    "Bye!"
                    )
                    return
                }
                if (result == Winner.STALEMATE) return
                continue
            }
            else -> return
        }
    }
}

fun checkIfThereIsAWinner(board: MutableList<MutableList<Char>>): Winner {
    board[7].forEach {
        if (it == 'W') {
            return Winner.PLAYER_ONE
        }
    }

    board[0].forEach {
        if (it == 'B') {
            return Winner.PLAYER_TWO
        }
    }

    var areThereAnyBlack = false
    var areThereAnyWhite = false
    run blackCheck@{
        board.forEach { currentRow ->
            currentRow.forEach { currentColumn ->
                if (currentColumn == 'B') {
                    areThereAnyBlack = true
                    return@blackCheck
                }
            }
        }
    }

    run whiteCheck@{
        board.forEach { currentRow ->
            currentRow.forEach { currentColumn ->
                if (currentColumn == 'W') {
                    areThereAnyWhite = true
                    return@whiteCheck
                }
            }
        }
    }

    if (!areThereAnyBlack) {
        return Winner.PLAYER_ONE
    }
    if (!areThereAnyWhite) {
        return Winner.PLAYER_TWO
    }

    if (isThereAStalemate(board)) {
        println(
                "Stalemate!\n" +
                        "Bye!"
        )
        return Winner.STALEMATE
    }

    return Winner.NONE
}

fun isThereAStalemate(board: MutableList<MutableList<Char>>): Boolean {

    val whitePawnConditionList = mutableListOf<Boolean>()
    val blackPawnConditionList = mutableListOf<Boolean>()

    for (rowIndex in board.indices) {
        for (columnIndex in board[rowIndex].indices) {
            val currentPawn = board[rowIndex][columnIndex]
            if (currentPawn == 'W') {
                val isValidOptionOne: Moves = try {
                    isAValidMove(
                            board,
                            currentPawn,
                            columnIndex,
                            rowIndex,
                            columnIndex - 1,
                            rowIndex + 1
                    )
                } catch (ex: Exception) {
                    Moves.INVALID_INPUT
                }
                val isValidOptionTwo: Moves = try {
                    isAValidMove(
                            board,
                            currentPawn,
                            columnIndex,
                            rowIndex,
                            columnIndex,
                            rowIndex + 1
                    )
                } catch (ex: Exception) {
                    Moves.INVALID_INPUT
                }
                val isValidOptionThree: Moves = try {
                    isAValidMove(
                            board,
                            currentPawn,
                            columnIndex,
                            rowIndex,
                            columnIndex + 1,
                            rowIndex + 1
                    )
                } catch (ex: Exception) {
                    Moves.INVALID_INPUT
                }

                whitePawnConditionList.add(
                        isValidOptionOne == Moves.VALID_MOVE ||
                                isValidOptionTwo == Moves.VALID_MOVE ||
                                isValidOptionThree == Moves.VALID_MOVE
                )
            }
            if (currentPawn == 'B') {
                val isValidOptionOne: Moves = try {
                    isAValidMove(
                            board,
                            currentPawn,
                            columnIndex,
                            rowIndex,
                            columnIndex - 1,
                            rowIndex - 1
                    )
                } catch (ex: Exception) {
                    Moves.INVALID_INPUT
                }
                val isValidOptionTwo: Moves = try {
                    isAValidMove(
                            board,
                            currentPawn,
                            columnIndex,
                            rowIndex,
                            columnIndex,
                            rowIndex - 1
                    )
                } catch (ex: Exception) {
                    Moves.INVALID_INPUT
                }
                val isValidOptionThree: Moves = try {
                    isAValidMove(
                            board,
                            currentPawn,
                            columnIndex,
                            rowIndex,
                            columnIndex + 1,
                            rowIndex - 1
                    )
                } catch (ex: Exception) {
                    Moves.INVALID_INPUT
                }
                blackPawnConditionList.add(
                        isValidOptionOne == Moves.VALID_MOVE ||
                                isValidOptionTwo == Moves.VALID_MOVE ||
                                isValidOptionThree == Moves.VALID_MOVE
                )
            }
        }
    }

    var isWhiteAbleToMove = false
    var isBlackAbleToMove = false
    whitePawnConditionList.forEach {
        if (it) {
            isWhiteAbleToMove = true
            return@forEach
        }
    }

    blackPawnConditionList.forEach {
        if (it) {
            isBlackAbleToMove = true
            return@forEach
        }
    }

    return !isWhiteAbleToMove || !isBlackAbleToMove
}

fun executeMoveForPLayer(
        statusGame: StatusGame,
        board: MutableList<MutableList<Char>>,
        playerMove: String
): Boolean {

    val xStart = playerMove.first().code - 97
    val yStart = playerMove[1].digitToInt() - 1
    val xEnd = playerMove[2].code - 97
    val yEnd = playerMove.last().digitToInt() - 1

    val moveState = isAValidMove(
            board,
            if (statusGame == StatusGame.PLAYER_ONE_ACTIVE) 'W' else 'B',
            xStart,
            yStart,
            xEnd,
            yEnd
    )

    return when (moveState) {
        Moves.VALID_MOVE -> {
            executeMove(statusGame, board, xStart, yStart, xEnd, yEnd)
            printBoard(board)
            true
        }
        Moves.NO_WHITE_PAWN -> {
            println("No white pawn at ${(xStart + 97).toChar()}${yStart + 1}")
            false
        }
        Moves.NO_BLACK_PAWN -> {
            println("No black pawn at ${(xStart + 97).toChar()}${yStart + 1}")
            false
        }
        Moves.INVALID_INPUT -> {
            println("Invalid Input")
            false
        }
    }
}

fun executeMove(
        statusGame: StatusGame,
        board: MutableList<MutableList<Char>>,
        xStart: Int,
        yStart: Int,
        xEnd: Int,
        yEnd: Int
) {

    //Delete the current pawn
    if (statusGame == StatusGame.PLAYER_ONE_ACTIVE) {
        board[yStart][xStart] = ' '
        board[yEnd][xEnd] = 'W'
        if (yStart == 1)
            addToDoubledMovesIfItWasFirstMove('W', yStart, yEnd, xStart)
        if (blackPawnHotSpot == cordsToString(xEnd, yEnd)) {
            deletePerviousPawn(xEnd, yEnd, 'W', board)
        }

    } else {
        board[yStart][xStart] = ' '
        board[yEnd][xEnd] = 'B'
        if (yStart == 6)
            addToDoubledMovesIfItWasFirstMove('B', yStart, yEnd, xStart)
        if (whitePawnHotSpot == cordsToString(xEnd, yEnd)) {
            deletePerviousPawn(xEnd, yEnd, 'B', board)
        }

    }

}

fun deletePerviousPawn(xEnd: Int, yEnd: Int, pawn: Char, board: MutableList<MutableList<Char>>) {
    if (pawn == 'W') {
        board[yEnd - 1][xEnd] = ' '
    } else {
        board[yEnd + 1][xEnd] = ' '
    }
}

fun addToDoubledMovesIfItWasFirstMove(pawn: Char, yStart: Int, yEnd: Int, xStart: Int) {
    whitePawnHotSpot = if (pawn == 'W' &&
            abs(yStart - yEnd) == 2
    ) {
        cordsToString(xStart, yStart + 1)
    } else {
        ""
    }


    blackPawnHotSpot = if (pawn == 'B' &&
            abs(yStart - yEnd) == 2
    ) {
        cordsToString(xStart, yStart + -1)
    } else {
        ""
    }


}

fun isAValidMove(
        board: MutableList<MutableList<Char>>,
        pawn: Char,
        xStart: Int,
        yStart: Int,
        xEnd: Int,
        yEnd: Int
): Moves {

    //Validate horizontal movement
    when {

        //There is no pawn in set spot
        pawn == 'W' && (board[yStart][xStart] != pawn) -> {
            /*println("No white pawn at ${(xStart + 97).toChar()}${yStart + 1}")*/
            return Moves.NO_WHITE_PAWN
        }
        pawn == 'B' && (board[yStart][xStart] != pawn) -> {
            return Moves.NO_BLACK_PAWN
        }
        //Invalid Movements
        pawn == 'W' && yEnd <= yStart -> {
            return Moves.INVALID_INPUT
        }
        pawn == 'B' && yEnd >= yStart -> {
            return Moves.INVALID_INPUT
        }

        pawn == 'W' && yStart == 1 && abs(yStart - yEnd) > 2 -> {
            return Moves.INVALID_INPUT
        }
        pawn == 'B' && yStart == 6 && abs(yStart - yEnd) > 2 -> {
            return Moves.INVALID_INPUT
        }

        isInvalidForwardMove(board, pawn, xStart, yStart, xEnd, yEnd) -> {
            return Moves.INVALID_INPUT
        }

        yStart == yEnd && board[yEnd][xEnd] != ' ' -> {
            return Moves.INVALID_INPUT
        }

        pawn == 'W' && yStart != 1 -> {
            if (abs(yStart - yEnd) > 1) {
                return Moves.INVALID_INPUT
            }
        }
        pawn == 'B' && yStart != 6 -> {
            if (abs(yStart - yEnd) > 1) {
                return Moves.INVALID_INPUT
            }
        }

        else -> return Moves.VALID_MOVE
    }
    return Moves.VALID_MOVE
}

/**
 * Will return false if the forward move is not able
 */
fun isInvalidForwardMove(
        board: MutableList<MutableList<Char>>,
        pawn: Char,
        xStart: Int,
        yStart: Int,
        xEnd: Int,
        yEnd: Int
): Boolean {

    if (pawn == 'W') {
        when {
            //EnPassant
            try {
                board[yStart][xStart - 1] == 'B'
            } catch (ex: Exception) {
                false
            }
                    && blackPawnHotSpot == cordsToString(
                    xEnd,
                    yEnd
            ) && xEnd == xStart - 1 && yEnd == yStart + 1 -> return false

            try {
                board[yStart][xStart + 1] == 'B'
            } catch (ex: Exception) {
                false
            }
                    && blackPawnHotSpot == cordsToString(
                    xEnd,
                    yEnd
            ) && xEnd == xStart + 1 && yEnd == yStart + 1 -> return false

            //Capture
            xEnd == xStart - 1 && board[yEnd][xEnd] == 'B' -> return false
            xEnd == xStart - 1 && board[yEnd][xEnd] == ' ' -> return true
            xEnd == xStart && board[yEnd][xEnd] == 'B' -> return true
            xEnd == xStart && board[yEnd][xEnd] == ' ' -> return false
            xEnd == xStart + 1 && board[yEnd][xEnd] == 'B' -> return false
            xEnd == xStart + 1 && board[yEnd][xEnd] == ' ' -> return true
        }
    } else {
        when {

            //EnPassant
            try {
                board[yStart][xStart - 1] == 'W'
            } catch (ex: Exception) {
                false
            }
                    && whitePawnHotSpot == cordsToString(
                    xEnd,
                    yEnd
            ) && xEnd == xStart - 1 && yEnd == yStart - 1 -> return false

            try {
                board[yStart][xStart + 1] == 'W'
            } catch (ex: Exception) {
                false
            }
                    && whitePawnHotSpot == cordsToString(
                    xEnd,
                    yEnd
            ) && xEnd == xStart + 1 && yEnd == yStart - 1 -> return false

            //Capture
            xEnd == xStart - 1 && board[yEnd][xEnd] == 'W' -> return false
            xEnd == xStart - 1 && board[yEnd][xEnd] == 'W' -> return true
            xEnd == xStart && board[yEnd][xEnd] == 'W' -> return true
            xEnd == xStart && board[yEnd][xEnd] == ' ' -> return false
            xEnd == xStart + 1 && board[yEnd][xEnd] == 'W' -> return false
            xEnd == xStart + 1 && board[yEnd][xEnd] == 'W' -> return true
        }
    }
    return true
}

fun cordsToString(x: Int, y: Int): String {
    val firstCord = (x + 97).toChar()
    return firstCord + y.toString()
}

fun askPlayerMove(statusGame: StatusGame): String {
    while (true) {
        println(
                "${
                    if (statusGame == StatusGame.PLAYER_ONE_ACTIVE) {
                        playerOneNickname
                    } else {
                        playerTwoNickname
                    }
                }'s turn:"
        )
        val userInput = readln()
        return when {
            userInput == "exit" -> {
                println("Bye!")
                userInput
            }
            userInputRegex.matches(userInput) -> userInput
            else -> {
                println("Invalid Input")
                continue
            }
        }
    }
}

fun definStartingBoard(): MutableList<MutableList<Char>> {
    val board = MutableList(8) {
        MutableList(8) {
            ' '
        }
    }
    board[1].replaceAll {
        'W'
    }
    board[6].replaceAll {
        'B'
    }
    return board
}

fun printBoard(board: MutableList<MutableList<Char>>) {

    for (rowIndex in board.indices.reversed()) {
        //Printing Separator
        printSeparatorWithDimention(board, rowIndex)
        //PrintContent
        print("${rowIndex + 1} ")
        for (columnIndex in board[rowIndex].indices) {
            print("| ${board[rowIndex][columnIndex]} ")
        }
        println("|")

        if (rowIndex == board.indices.first) {
            printSeparatorWithDimention(board, rowIndex)
            //Printing the low label of letters
            print("  ")
            for (columnIndex in board[rowIndex].indices) {
                print("  ${(columnIndex + 97).toChar()} ")
            }
        }
    }
    println("")
}

fun printSeparatorWithDimention(board: MutableList<MutableList<Char>>, rowIndex: Int) {
    print("  ")
    for (columnIndex in board[rowIndex].indices) {
        print("+---")
    }
    println("+")
}