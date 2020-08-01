package main.application;

public class Event implements java.io.Serializable{
    private String title, sport;
    private Game game;

    public Event(String title, String sport, Game game) {
        setTitle(title);
        setSport(sport);
        setGame(game);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSport() {
        return sport;
    }

    public void setSport(String sport) {
        this.sport = sport;
    }

    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    /**
     * Creates Description for the event
     * @return String: title + ": " + game.toString()
     */
    public String getDescription(){
        return this.getTitle().concat(": ").concat(getGame().toString());
    }
}
