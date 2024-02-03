package com.example.esportsevent

data class ApiResponse(val response: List<Match>)

data class LeagueApiResponse(
    val response: List<LeaguesOfMatchViewer>
)

data class LeaguesOfMatchViewer(
    val league: Leagues,
    val country: Country,
    val seasons: List<Season>
)

data class Leagues(
    val id: Int,
    val name: String,
    val type: String,
    val logo: String
)

data class Country(
    val name: String,
    val code: String?,
    val flag: String?
)

data class Season(
    val year: Int,
    val start: String,
    val end: String,
    val current: Boolean,
    val coverage: Coverage
)

data class Coverage(
    val fixtures: Fixtures,
    val standings: Boolean,
    // ... other fields
)

data class Fixtures(
    val events: Boolean,
    val lineups: Boolean,
    // ... other fields
)


data class Match(
    val fixture: Fixture,
    val league: League,
    val teams: Teams,
    val goals: Goals,
    val score: Score
)

data class Fixture(
    val id: Int,
    val referee: String?,
    val timezone: String,
    val date: String,
    val timestamp: Long,
    val venue: Venue,
    val status: Status
)

data class Venue(val id: Int, val name: String, val city: String)

data class Status(val long: String, val short: String, val elapsed: Int?)

data class League(val id: Int, val name: String, val country: String, val logo: String, val flag: String?, val season: Int, val round: String)

data class Teams(val home: Team, val away: Team)

data class Team(val id: Int, val name: String, val logo: String, val winner: Boolean?)

data class Goals(val home: Int?, val away: Int?)

data class Score(val halftime: ScoreDetail, val fulltime: ScoreDetail, val extratime: ScoreDetail?, val penalty: ScoreDetail?)

data class ScoreDetail(val home: Int?, val away: Int?)

data class TeamStatisticsResponse(
    val get: String,
    val parameters: Parameters,
    val errors: List<Any>,
    val results: Int,
    val paging: Paging,
    val response: Response
)

data class Parameters(
    val league: String,
    val season: String,
    val team: String
)

data class Paging(
    val current: Int,
    val total: Int
)

data class Response(
    val league: TeamLeague,
    val team: TeamInfo,
    val form: String,
    val fixtures: TeamFixtures,
    val goals: TeamGoals,
    val biggest: Biggest,
    val clean_sheet: CleanSheet,
    val failed_to_score: FailedToScore,
    val penalty: Penalty,
    val lineups: List<Lineup>,
    val cards: Cards
)

data class TeamLeague(
    val id: Int,
    val name: String,
    val country: String,
    val logo: String,
    val flag: String,
    val season: Int
)

data class TeamInfo(
    val id: Int,
    val name: String,
    val logo: String
)

data class TeamFixtures(
    val played: Played,
    val wins: Wins,
    val draws: Draws,
    val loses: Loses
)

data class Played(
    val home: Int,
    val away: Int,
    val total: Int
)

data class Wins(
    val home: Int,
    val away: Int,
    val total: Int
)

data class Draws(
    val home: Int,
    val away: Int,
    val total: Int
)

data class Loses(
    val home: Int,
    val away: Int,
    val total: Int
)

data class TeamGoals(
    val `for`: ForAgainst,
    val against: ForAgainst
)

data class ForAgainst(
    val total: Total,
    val average: Average,
    val minute: Map<String, MinuteDetail>
)

data class Total(
    val home: Int,
    val away: Int,
    val total: Int
)

data class Average(
    val home: String,
    val away: String,
    val total: String
)

data class MinuteDetail(
    val total: Int?,
    val percentage: String?
)

data class Biggest(
    val streak: Streak,
    val wins: HomeAway,
    val loses: HomeAway,
    val goals: GoalsBiggest
)

data class Streak(
    val wins: Int,
    val draws: Int,
    val loses: Int
)

data class HomeAway(
    val home: String,
    val away: String
)

data class GoalsBiggest(
    val `for`: HomeAwayInt,
    val against: HomeAwayInt
)

data class HomeAwayInt(
    val home: Int,
    val away: Int
)

data class CleanSheet(
    val home: Int,
    val away: Int,
    val total: Int
)

data class FailedToScore(
    val home: Int,
    val away: Int,
    val total: Int
)

data class Penalty(
    val scored: ScoredMissed,
    val missed: ScoredMissed,
    val total: Int
)

data class ScoredMissed(
    val total: Int,
    val percentage: String
)

data class Lineup(
    val formation: String,
    val played: Int
)

data class Cards(
    val yellow: Map<String, CardDetail>,
    val red: Map<String, CardDetail>
)

data class CardDetail(
    val total: Int?,
    val percentage: String?
)
