package parser;

import command.AddCommand;
import command.ClearCommand;
import command.Command;
import command.DeleteCommand;
import command.DoneCommand;
import command.EditCommand;
import command.ExitCommand;
import command.FindCommand;
import command.FindDateCommand;
import command.HelpCommand;
import command.LocateCommand;
import command.PrintFullListCommand;
import command.PrintLocationCommand;
import command.ReminderCommand;
import command.SortCommand;

import event.Assignment;
import event.PersonalEvent;
import event.Assignment;
import event.Class;
import event.PersonalEvent;
import exception.DateFormatException;
import exception.DeleteNumberFormatException;
import exception.DoneNumberFormatException;
import exception.EmptyDeleteException;
import exception.EmptyDoneException;
import exception.EmptyEventException;
import exception.EmptyEventIndexException;
import exception.EmptyFindDateException;
import exception.EmptyFindException;
import exception.InvalidSortCriteriaException;
import exception.NoEndTimeClassException;
import exception.NoEditEventDescriptionException;

import exception.NoEventLocationException;
import exception.NoEventTimeException;
import exception.NoEventTimeMarkerException;
import exception.NoSortCriteriaException;
import exception.NuScheduleException;
import exception.TimeFormatException;
import exception.UnknownErrorException;
import exception.WrongCommandException;
import exception.WrongEditFormatException;
import location.Building;
import location.Hostel;
import location.LectureTheatre;
import location.Location;
import location.OnlineLocation;
import location.OutOfNuS;
import locationlist.LocationList;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;


/**
 * This class contains one function -- parse, to call the respective command function according to the user input.
 */
public abstract class Parser {
    public static final String EXIT = "bye";
    public static final String PRINT_EVENT_LIST = "list";
    public static final String PRINT_LOCATION_LIST = "locations";
    public static final String LOCATE_EVENT = "locate";
    public static final String EVENT_DONE = "done";
    public static final String ASSIGNMENT = "assignment";
    public static final String CLASS = "class";
    public static final String PERSONAL_EVENT = "personalEvent";
    public static final String EVENT_DELETE = "delete";
    public static final String EVENT_FIND = "find";
    public static final String EDIT = "edit";
    public static final String TIME_MARKER = "/t";
    public static final String SINGLE_SPACE = " ";
    public static final String LOCATION_MARKER = "/l";
    public static final String EVENT_FIND_DATE = "date";
    public static final String HELP = "help";
    public static final String CLEAR = "clear";
    public static final String SORT = "sort";
    public static final String END_TIME_MARKER = "/e";
    public static final String ONLINE_LOCATION_MARKER = "/o";
    public static final String PASSWORD_MARKER = "/p";
    public static final String REMIND = "reminder";

    /**
     * This function calls the correct command the user want to perform, by returning a Command object.
     *
     * @param fullCommand the full string of user input
     * @return the specific Command object to perform what the user want to do
     * @throws NuScheduleException includes all exceptions may happen during parsing
     */

    public static Command parse(String fullCommand, LocationList locations) throws NuScheduleException {

        //deletes all the starting and ending spaces
        fullCommand = fullCommand.trim();
        // this block deals with single word command
        switch (fullCommand) {
        case EXIT:
            return new ExitCommand();
        case PRINT_EVENT_LIST:
            return new PrintFullListCommand();
        case PRINT_LOCATION_LIST:
            return new PrintLocationCommand();
        case HELP:
            return new HelpCommand();
        case CLEAR:
            return new ClearCommand();
        default:
            break;
        }

        String[] words = fullCommand.split(SINGLE_SPACE);

        //this block deals with locate command
        if (words[0].equals(LOCATE_EVENT)) {
            return new LocateCommand(words[1]);
        }

        //this block deals with find command
        if (words[0].equals(EVENT_FIND)) {
            if (fullCommand.substring(4).isBlank()) {
                throw new EmptyFindException();
            }
            return new FindCommand(fullCommand.substring(5));
        }

        //this block deals with find date command
        if (words[0].equals(EVENT_FIND_DATE)) {
            if (fullCommand.substring(4).isBlank()) {
                throw new EmptyFindDateException();
            }
            try {
                return new FindDateCommand(LocalDate.parse(fullCommand.substring(5)));
            } catch (DateTimeParseException e) {
                throw new DateFormatException();
            }
        }

        if (words[0].equals(REMIND)) {
            return new ReminderCommand();
        }

        int eventIndex;//to indicate what is the Event we are dealing with. may not be used.

        //this block deals with done command
        if (words[0].equals(EVENT_DONE)) {
            if (fullCommand.substring(4).isBlank()) {
                throw new EmptyDoneException();
            }
            try {
                eventIndex = Integer.parseInt(fullCommand.substring(5)) - 1;
            } catch (NumberFormatException e) {
                throw new DoneNumberFormatException();
            }
            return new DoneCommand(eventIndex);
        }

        //this block deals with delete command
        if (words[0].equals(EVENT_DELETE)) {
            if (fullCommand.substring(6).isBlank()) {
                throw new EmptyDeleteException();
            }
            try {
                eventIndex = Integer.parseInt(fullCommand.substring(7)) - 1;
            } catch (NumberFormatException e) {
                throw new DeleteNumberFormatException();
            }
            return new DeleteCommand(eventIndex);
        }

        //this block deals with sorting
        if (words[0].equals(SORT)) {
            if (fullCommand.length() == 4) {
                throw new NoSortCriteriaException();
            }
            String type = words[1];
            switch (type) {
            case "description":
            case "time":
            case "location":
                return new SortCommand(type);
            default:
                throw new InvalidSortCriteriaException();
            }
        }


        //these variables are used by either Edit or Add
        //the position of /t
        int startTimeDividerPosition = fullCommand.indexOf(TIME_MARKER);
        ;
        //the position of the space when the user enters a date time in the format yyyy-mm-dd HH:mm
        int timeDivider;
        //the position of /et
        int endTimeDividerPosition = fullCommand.indexOf(END_TIME_MARKER);
        //the position of /l
        int locationDividerPosition = fullCommand.indexOf(LOCATION_MARKER);
        //the position of /o
        int onlineLocationDividerPosition = fullCommand.indexOf(ONLINE_LOCATION_MARKER);
        //the position of /p
        int pwdDividerPosition = fullCommand.indexOf(PASSWORD_MARKER);
        String startDateTime;
        Location location;
        String endDateTime;
        //the position of the space when the user enters an ending date time in the format yyyy-mm-dd HH:mm
        int endingTimeDivider;


        //this block deals with edit command
        //this block will change fullCommand, but this does not affect the later block since
        //it either return an EditCommand, or throw an exception
        if (words[0].equals(EDIT)) {

            if (fullCommand.length() == 4) {
                throw new EmptyEventIndexException();
            }
            if (fullCommand.substring(5).isBlank()) {
                throw new EmptyEventIndexException();
            }
            try {
                eventIndex = Integer.parseInt(words[1]) - 1;
            } catch (NumberFormatException e) {
                throw new WrongEditFormatException();
            }

            //the following part is almost the same as AddCommand, but returns EditCommand
            if (words.length == 2) {
                throw new NoEditEventDescriptionException();
            }

            if (startTimeDividerPosition == -1) {
                throw new NoEventTimeMarkerException();
            }


            if (words[2].equals(ASSIGNMENT) || words[2].equals(CLASS) || words[2].equals(PERSONAL_EVENT)) {

                if (locationDividerPosition == -1 && onlineLocationDividerPosition == -1) {
                    throw new NoEventLocationException();
                }

                int prefixLength = words[0].length() + words[1].length() + words[2].length();


                if (fullCommand.substring(prefixLength, startTimeDividerPosition).isBlank()) {
                    throw new EmptyEventException();
                }

                if (locationDividerPosition != -1) {
                    if (fullCommand.substring(startTimeDividerPosition + 3, locationDividerPosition - 1).isBlank()) {
                        throw new NoEventTimeException();
                    }
                    if (fullCommand.substring(locationDividerPosition + 3).isBlank()) {
                        throw new NoEventLocationException();
                    }
                } else {
                    if (fullCommand.substring(startTimeDividerPosition + 3, onlineLocationDividerPosition - 1)
                            .isBlank()) {
                        throw new NoEventTimeException();
                    }
                    if (fullCommand.substring(onlineLocationDividerPosition + 3).isBlank()) {
                        throw new NoEventLocationException();
                    }
                }

                //this deals with the event holding offline
                if (locationDividerPosition != -1) {
                    try {
                        timeDivider = fullCommand.substring(startTimeDividerPosition + 3).indexOf(SINGLE_SPACE);
                        location = parseLocation(fullCommand.substring(locationDividerPosition + 3), locations);

                        switch (words[2]) {
                        case ASSIGNMENT:
                            startDateTime = getStartDateTime(fullCommand, startTimeDividerPosition, timeDivider,
                                    locationDividerPosition);

                            return new EditCommand(new Assignment(fullCommand.substring(prefixLength + 1,
                                    startTimeDividerPosition - 1), location, LocalDateTime.parse(startDateTime)),
                                    eventIndex);
                        case CLASS:
                            if (endTimeDividerPosition == -1) {
                                throw new NoEndTimeClassException();
                            }
                            startDateTime = getStartDateTime(fullCommand, startTimeDividerPosition, timeDivider,
                                    endTimeDividerPosition);

                            endingTimeDivider = fullCommand.substring(endTimeDividerPosition + 3,
                                    locationDividerPosition - 1).indexOf(SINGLE_SPACE);

                            //if the user does not input the date of the ending time, by default it ends at the same
                            // day as the starting time
                            endDateTime = getEndDateTime(fullCommand, startTimeDividerPosition, timeDivider,
                                    endTimeDividerPosition, locationDividerPosition, endingTimeDivider);

                            return new EditCommand(new Class(fullCommand.substring(prefixLength + 1,
                                    startTimeDividerPosition - 1), location, LocalDateTime.parse(startDateTime),
                                    LocalDateTime.parse(endDateTime)),
                                    eventIndex);
                        case PERSONAL_EVENT:
                            if (endTimeDividerPosition == -1) {
                                startDateTime = getStartDateTime(fullCommand, startTimeDividerPosition, timeDivider,
                                        locationDividerPosition);
                                return new EditCommand(new PersonalEvent(fullCommand.substring(prefixLength + 1,
                                        startTimeDividerPosition - 1), location, LocalDateTime.parse(startDateTime)),
                                        eventIndex);
                            } else {
                                startDateTime = getStartDateTime(fullCommand, startTimeDividerPosition, timeDivider,
                                        endTimeDividerPosition);

                                endingTimeDivider = fullCommand.substring(endTimeDividerPosition + 3,
                                        locationDividerPosition - 1).indexOf(SINGLE_SPACE);

                                //if the user does not input the date of the ending time, by default it ends at the
                                // same day as the starting time
                                endDateTime = getEndDateTime(fullCommand, startTimeDividerPosition, timeDivider,
                                        endTimeDividerPosition, locationDividerPosition, endingTimeDivider);

                                return new EditCommand(new PersonalEvent(fullCommand.substring(prefixLength + 1,
                                        startTimeDividerPosition - 1), location, LocalDateTime.parse(startDateTime),
                                        LocalDateTime.parse(endDateTime)),
                                        eventIndex);
                            }
                        default:
                            break;
                        }
                    } catch (DateTimeParseException | StringIndexOutOfBoundsException e) {
                        throw new TimeFormatException();
                    }
                } else {
                    try {
                        timeDivider = fullCommand.substring(startTimeDividerPosition + 3).indexOf(SINGLE_SPACE);
                        OnlineLocation virtualLocation;
                        if (pwdDividerPosition == -1) {
                            virtualLocation =
                                    new OnlineLocation(fullCommand.substring(onlineLocationDividerPosition + 3));
                        } else {
                            virtualLocation =
                                    new OnlineLocation(fullCommand.substring(onlineLocationDividerPosition + 3,
                                            pwdDividerPosition - 1), fullCommand.substring(pwdDividerPosition + 3));
                        }

                        switch (words[2]) {
                        case ASSIGNMENT:
                            startDateTime = getStartDateTime(fullCommand, startTimeDividerPosition, timeDivider,
                                    onlineLocationDividerPosition);

                            return new EditCommand(new Assignment(fullCommand.substring(prefixLength + 1,
                                    startTimeDividerPosition - 1), virtualLocation, LocalDateTime.parse(startDateTime)),
                                    eventIndex);
                        case CLASS:
                            if (endTimeDividerPosition == -1) {
                                throw new NoEndTimeClassException();
                            }
                            startDateTime = getStartDateTime(fullCommand, startTimeDividerPosition, timeDivider,
                                    endTimeDividerPosition);

                            endingTimeDivider = fullCommand.substring(endTimeDividerPosition + 3,
                                    onlineLocationDividerPosition - 1).indexOf(SINGLE_SPACE);

                            endDateTime = getEndDateTime(fullCommand, startTimeDividerPosition, timeDivider,
                                    endTimeDividerPosition, onlineLocationDividerPosition, endingTimeDivider);

                            return new EditCommand(new Class(fullCommand.substring(prefixLength + 1,
                                    startTimeDividerPosition - 1), virtualLocation, LocalDateTime.parse(startDateTime),
                                    LocalDateTime.parse(endDateTime)),
                                    eventIndex);
                        case PERSONAL_EVENT:
                            if (endTimeDividerPosition == -1) {
                                startDateTime = getStartDateTime(fullCommand, startTimeDividerPosition, timeDivider,
                                        onlineLocationDividerPosition);
                                return new EditCommand(new PersonalEvent(fullCommand.substring(prefixLength + 1,
                                        startTimeDividerPosition - 1), virtualLocation,
                                        LocalDateTime.parse(startDateTime)),
                                        eventIndex);
                            } else {
                                startDateTime = getStartDateTime(fullCommand, startTimeDividerPosition, timeDivider,
                                        endTimeDividerPosition);

                                endingTimeDivider = fullCommand.substring(endTimeDividerPosition + 3,
                                        onlineLocationDividerPosition - 1).indexOf(SINGLE_SPACE);

                                endDateTime = getEndDateTime(fullCommand, startTimeDividerPosition, timeDivider,
                                        endTimeDividerPosition, onlineLocationDividerPosition, endingTimeDivider);

                                return new EditCommand(new PersonalEvent(fullCommand.substring(prefixLength + 1,
                                        startTimeDividerPosition - 1), virtualLocation,
                                        LocalDateTime.parse(startDateTime),
                                        LocalDateTime.parse(endDateTime)),
                                        eventIndex);
                            }
                        default:
                            break;
                        }
                    } catch (DateTimeParseException | StringIndexOutOfBoundsException e) {
                        throw new TimeFormatException();
                    }

                }
            } else {
                throw new WrongCommandException();
            }

        }

        //this block deals with add command
        //we shall check that the user input is not meant for any other command beforehand
        //because the default block will throw an exception.
        // i.e. when this block is entered, the parser will not go to any other blocks
        if (words[0].equals(ASSIGNMENT) || words[0].equals(CLASS) || words[0].equals(PERSONAL_EVENT)) {
            if (fullCommand.substring(words[0].length()).isBlank()) {
                throw new EmptyEventException();
            }

            if (startTimeDividerPosition == -1) {
                throw new NoEventTimeMarkerException();
            }

            if (locationDividerPosition == -1 && onlineLocationDividerPosition == -1) {
                throw new NoEventLocationException();
            }

            if (fullCommand.substring(words[0].length(), startTimeDividerPosition).isBlank()) {
                throw new EmptyEventException();
            }

            if (locationDividerPosition != -1) {
                if (fullCommand.substring(startTimeDividerPosition + 3, locationDividerPosition - 1).isBlank()) {
                    throw new NoEventTimeException();
                }
                if (fullCommand.substring(locationDividerPosition + 3).isBlank()) {
                    throw new NoEventLocationException();
                }
            } else {
                if (fullCommand.substring(startTimeDividerPosition + 3, onlineLocationDividerPosition - 1)
                        .isBlank()) {
                    throw new NoEventTimeException();
                }
                if (fullCommand.substring(onlineLocationDividerPosition + 3).isBlank()) {
                    throw new NoEventLocationException();
                }
            }


            //this deals with the event holding offline
            if (locationDividerPosition != -1) {
                if (fullCommand.substring(locationDividerPosition + 3).isBlank()) {
                    throw new NoEventLocationException();
                }
                try {
                    timeDivider = fullCommand.substring(startTimeDividerPosition + 3).indexOf(SINGLE_SPACE);
                    location = parseLocation(fullCommand.substring(locationDividerPosition + 3), locations);

                    switch (words[0]) {
                    case ASSIGNMENT:
                        startDateTime = getStartDateTime(fullCommand, startTimeDividerPosition, timeDivider,
                                locationDividerPosition);

                        return new AddCommand(new Assignment(fullCommand.substring(words[0].length() + 1,
                                startTimeDividerPosition - 1), location, LocalDateTime.parse(startDateTime)));
                    case CLASS:
                        if (endTimeDividerPosition == -1) {
                            throw new NoEndTimeClassException();
                        }

                        startDateTime = getStartDateTime(fullCommand, startTimeDividerPosition, timeDivider,
                                endTimeDividerPosition);

                        endingTimeDivider = fullCommand.substring(endTimeDividerPosition + 3,
                                locationDividerPosition - 1).indexOf(SINGLE_SPACE);

                        //if the user does not input the date of the ending time, by default it ends at the same
                        // day as the starting time
                        endDateTime = getEndDateTime(fullCommand, startTimeDividerPosition, timeDivider,
                                endTimeDividerPosition, locationDividerPosition, endingTimeDivider);

                        return new AddCommand(new Class(fullCommand.substring(words[0].length() + 1,
                                startTimeDividerPosition - 1), location, LocalDateTime.parse(startDateTime),
                                LocalDateTime.parse(endDateTime)));
                    case PERSONAL_EVENT:
                        if (endTimeDividerPosition == -1) {
                            startDateTime = getStartDateTime(fullCommand, startTimeDividerPosition, timeDivider,
                                    locationDividerPosition);
                            return new AddCommand(new PersonalEvent(fullCommand.substring(words[0].length() + 1,
                                    startTimeDividerPosition - 1), location, LocalDateTime.parse(startDateTime)));
                        } else {
                            startDateTime = getStartDateTime(fullCommand, startTimeDividerPosition, timeDivider,
                                    endTimeDividerPosition);

                            endingTimeDivider = fullCommand.substring(endTimeDividerPosition + 3,
                                    locationDividerPosition - 1).indexOf(SINGLE_SPACE);

                            //if the user does not input the date of the ending time, by default it ends at the same
                            // day as the starting time
                            endDateTime = getEndDateTime(fullCommand, startTimeDividerPosition, timeDivider,
                                    endTimeDividerPosition, locationDividerPosition, endingTimeDivider);

                            return new AddCommand(new PersonalEvent(fullCommand.substring(words[0].length() + 1,
                                    startTimeDividerPosition - 1), location, LocalDateTime.parse(startDateTime),
                                    LocalDateTime.parse(endDateTime)));
                        }
                    default:
                        break;
                    }
                } catch (DateTimeParseException | StringIndexOutOfBoundsException e) {
                    throw new TimeFormatException();

                }
            } else { //this deals with the event holding online
                try {
                    timeDivider = fullCommand.substring(startTimeDividerPosition + 3).indexOf(SINGLE_SPACE);
                    OnlineLocation virtualLocation;
                    if (pwdDividerPosition == -1) {
                        virtualLocation =
                                new OnlineLocation(fullCommand.substring(onlineLocationDividerPosition + 3));
                    } else {
                        virtualLocation =
                                new OnlineLocation(fullCommand.substring(onlineLocationDividerPosition + 3,
                                        pwdDividerPosition - 1), fullCommand.substring(pwdDividerPosition + 3));
                    }

                    switch (words[0]) {
                    case ASSIGNMENT:
                        startDateTime = getStartDateTime(fullCommand, startTimeDividerPosition, timeDivider,
                                onlineLocationDividerPosition);

                        return new AddCommand(new Assignment(fullCommand.substring(words[0].length() + 1,
                                startTimeDividerPosition - 1), virtualLocation, LocalDateTime.parse(startDateTime)));
                    case CLASS:
                        if (endTimeDividerPosition == -1) {
                            throw new NoEndTimeClassException();
                        }
                        startDateTime = getStartDateTime(fullCommand, startTimeDividerPosition, timeDivider,
                                endTimeDividerPosition);

                        endingTimeDivider = fullCommand.substring(endTimeDividerPosition + 3,
                                onlineLocationDividerPosition - 1).indexOf(SINGLE_SPACE);

                        endDateTime = getEndDateTime(fullCommand, startTimeDividerPosition, timeDivider,
                                endTimeDividerPosition, onlineLocationDividerPosition, endingTimeDivider);

                        return new AddCommand(new Class(fullCommand.substring(words[0].length() + 1,
                                startTimeDividerPosition - 1), virtualLocation, LocalDateTime.parse(startDateTime),
                                LocalDateTime.parse(endDateTime)));
                    case PERSONAL_EVENT:
                        if (endTimeDividerPosition == -1) {
                            startDateTime = getStartDateTime(fullCommand, startTimeDividerPosition, timeDivider,
                                    onlineLocationDividerPosition);
                            return new AddCommand(new PersonalEvent(fullCommand.substring(words[0].length() + 1,
                                    startTimeDividerPosition - 1), virtualLocation,
                                    LocalDateTime.parse(startDateTime)));
                        } else {
                            startDateTime = getStartDateTime(fullCommand, startTimeDividerPosition, timeDivider,
                                    endTimeDividerPosition);

                            endingTimeDivider = fullCommand.substring(endTimeDividerPosition + 3,
                                    onlineLocationDividerPosition - 1).indexOf(SINGLE_SPACE);

                            endDateTime = getEndDateTime(fullCommand, startTimeDividerPosition, timeDivider,
                                    endTimeDividerPosition, onlineLocationDividerPosition, endingTimeDivider);

                            return new AddCommand(new PersonalEvent(fullCommand.substring(words[0].length() + 1,
                                    startTimeDividerPosition - 1), virtualLocation,
                                    LocalDateTime.parse(startDateTime),
                                    LocalDateTime.parse(endDateTime)));
                        }
                    default:
                        break;
                    }
                } catch (DateTimeParseException | StringIndexOutOfBoundsException e) {
                    throw new TimeFormatException();
                }

            }
        } else {
            throw new WrongCommandException();
        }

        assert false;//nothing should reach here
        throw new UnknownErrorException();
    }

    /**
     * return the EndDateTime. if the date is not specified, by default, it ends at the same date as the starting date.
     *
     * @param fullCommand              the full command provided by user
     * @param startTimeDividerPosition index of "/t"
     * @param timeDivider              index of " " in the start time
     * @param endTimeDividerPosition   index of "/e"
     * @param locationDividerPosition  index of "/o" or "/l"
     * @param endingTimeDivider        index of " " in the end date time, may be -1
     * @return a string in the format "yyyy-MM-dd HH:mm" that can be parsed into a LocalDateTime object
     */
    private static String getEndDateTime(String fullCommand, int startTimeDividerPosition, int timeDivider,
                                         int endTimeDividerPosition, int locationDividerPosition,
                                         int endingTimeDivider) {
        return (endingTimeDivider != -1 ? fullCommand.substring(endTimeDividerPosition + 3,
                endTimeDividerPosition + 3 + endingTimeDivider) :
                fullCommand.substring(startTimeDividerPosition + 3,
                        startTimeDividerPosition + 3 + timeDivider))
                + "T"
                + (endingTimeDivider != -1 ? fullCommand.substring(endTimeDividerPosition + 3 + endingTimeDivider + 1,
                locationDividerPosition - 1) : fullCommand.substring(endTimeDividerPosition + 3,
                locationDividerPosition - 1));
    }

    /**
     * return the StartDateTime.
     *
     * @param fullCommand              the full command provided by user
     * @param startTimeDividerPosition index of "/t"
     * @param timeDivider              index of " " in the start time
     * @param endTimeDividerPosition   index of "/e"
     * @return a string in the format "yyyy-MM-dd HH:mm" that can be parsed into a LocalDateTime object
     */
    private static String getStartDateTime(String fullCommand, int startTimeDividerPosition, int timeDivider,
                                           int endTimeDividerPosition) {
        return fullCommand.substring(startTimeDividerPosition + 3,
                startTimeDividerPosition + 3 + timeDivider)
                + "T"
                + fullCommand.substring(startTimeDividerPosition + 3 + timeDivider + 1,
                endTimeDividerPosition - 1);
    }

    /**
     * This method parses the inputted location.
     *
     * @param input the string inputted by the user.
     * @return the parsed location.
     */
    public static Location parseLocation(String input, LocationList locations) {
        assert locations != null;
        Location location;
        String[] info = input.split("/");
        // parse location from event.txt file
        try {
            String[] additionalInfo = info[2].split(",");
            switch (info[0]) {
            case "BLK":
                location = new Building(info[1], additionalInfo);
                break;
            case "H":
                location = new Hostel(info[1], additionalInfo);
                break;
            case "L":
                location = new LectureTheatre(info[1], info[2]);
                break;
            case "OUT":
                location = new OutOfNuS(info[1]);
                break;
            default:
                location = new OutOfNuS(info[0]);
                break;
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            if (input.contains("/")) {
                location = new OutOfNuS(info[1]);
                locations.getLocationList().add(location);
            }
            // parse location from user input
            // System.out.print("Invalid Location Format.");
            location = locations.findLocation(input.trim());
            if (location == null) {
                location = new OutOfNuS(input.trim());
            }
        }
        return location;
    }
}

