# Adjust

Adjust is a desktop application that provides spaced-repetition flash cards for chess openings. It is directly inspired by Anki, which is a general-purpose flash card application on desktop and Android. Adjust implements the SuperMemo2 algorithm, a modified version of which is also used by Anki. 

The SuperMemo2 algorithm is an open-source, free-to-use algorithm developed by SuperMemo World. To learn more, please visit the following website: 

https://www.supermemo.com
Algorithm SM-2, (C) Copyright SuperMemo World, 1991.

The application also currently comes pre-loaded with a variety of chess opening moves, all sourced from the open-source lichess.org/chess-openings project, which has worked to collect the plethora of named opening sequences that exist. That project (github.com/lichess-org/chess-openings) is released under the following open-source license: Creative Commons Zero v1.0 Universal.

# Main Menu

The main menu allows you to create, delete, and rename decks. Each deck will display a list of cards with some cards being "due" for review. To add cards to your deck, first create a deck, then select that deck and hit "modify."

# Deck Builder Menu

The deck builder menu comes with preloaded chess lines, each displayed in the top panel. To add moves from those lines into your deck, select the line(s) and hit the "Make Card(s)" button. The program will turn either each of the white moves or each of the black moves into their own card, depending on the option you select. Each card displays the sequence in the opening, th eopening name, and the time you last reviewed the card.

The Deck Builder Menu automatically excludes duplicates. So, if you add two lines containing "1. e4" to the same deck, only one card will be added.

If you wish to remove cards from your deck, select the card(s) and hit the "Delete Card(s)" button. If you delete a card, your progress on that card will be deleted, and the program will forget that that card was ever in your deck.

# Review Menu

To review the chess positions you've selected, pick a deck and click "REVIEW." You will be prompted with a chess position. To study the position, make your best guess of what you think the next move should be. 

When you have an answer in mind, click "Show Answer." The program will now show you the correct move, given the specific line you are studying. To see the next card, give yourself a grade from 0-5. Grade 5 indicates the card was easy and grade 0 indicates the card was difficult. Grades 0-2 are considered failing, whereas grades 3-5 are considered passing. The program will use the grade in deciding how long to wait before showing you the same card again.

A key feature of effective memorization is active recall, where you play an active role in producing a fact from memory. In the future, Adjust will be expanded to allow you to drag and drop chess pieces into new positions, which will aid in active recall. For now, it may be beneficial to say a move out loud before you hit "Show Answer," or write it down. 

# Efficient Studying

The conventional wisdom in the chess community is that, for most players, the area most in need of improvement is tactics and not openings. I do not disagree. If you are seeking to improve as a chess player (especially if you are starting from a lower ELO rating), you will probably get the highest marginal return from studying tactics and fundamental principles rather than studying openings. However, if you have reached a point in your studies where you want to learn some chess openings, I hope this program can be of use to you.
