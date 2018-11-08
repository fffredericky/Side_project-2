package com.example.jimmy.sideproject1;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

/**
 * A infoSlicer that performs various slicing of course information, as provided by the textScraper
 * class.
 */
public final class infoSlicer {

    /**
     * Return a sliced list of lecture info.
     *
     * PRECONDITION:     courseInfo is a list returned by getCourseInfo.
     * @param courseInfo a list of all the course info, scraped on the U of T coursefinder site.
     * @return           a list of lists, where each sublist contains one specific lecture's info.
     */
    private static List<List<String>> sliceCourseInfo(List<String> courseInfo){
        List<List<String>> slicedInfo = new ArrayList<>();
        List<String> curLecSlice = new ArrayList<>();
        for (String i: courseInfo){
            if(i.contains("Lec ")) {
                slicedInfo.add(curLecSlice);
                curLecSlice = new ArrayList<>();
            // Ignore tutorials for now.
            } else if (i.contains("Tut ")) {
                break;
            }
            curLecSlice.add(i);
        }
        // Add the final slice
        slicedInfo.add(curLecSlice);
        // First element is just an empty list.
        slicedInfo.remove(0);
        return slicedInfo;
    }

    /**
     * Returns a list of the the broken up dateTime, in the order (Day, Start, End) repeated.
     * @param dateTime a string representation of the date and time, according to the course finder
     *                 format.
     * @return         a list of the broken up dateTime string.
     */
    private static List<String> breakUpDateTime(String dateTime){
        List brokenUp = new ArrayList();
        boolean wasColon = false;
        String curString = "";
        for(int i = 0; i < dateTime.length(); i++){
            char c = dateTime.charAt(i);
            // Add the current string
            if(c == ' ') {
                if(!curString.equals("")) {
                    brokenUp.add(curString);
                }
                curString = "";
                wasColon = false;
            // Add the current start time
            } else if(c == ':') {
                brokenUp.add(curString);
                curString = "";
                wasColon = true;
            } else {
                // Not a in between start and end time char
                if(wasColon == false){
                    curString += c;
                } else{
                    // The last in between start and end time char
                    if(c == '-'){
                        wasColon = false;
                    }
                }
            }
        }
        return brokenUp;
    }

    /**
     * Return a 2-d list, where we have grouped the required information based on groupSize
     * @param brokenUp  The broken-up string in list form to group.
     * @param groupSize The size of each group.
     * @return          The list of grouping.
     */
    private static List<List<String>> groupBrokenString(List<String> brokenUp, int groupSize){
        List<List<String>> fullyGrouped = new ArrayList<>();
        List<String> curGroup = new ArrayList<>();
        for(String i: brokenUp){
            curGroup.add(i);
            if(curGroup.size() == groupSize){
                fullyGrouped.add(curGroup);
                curGroup = new ArrayList<>();
            }
        }
        return fullyGrouped;
    }

    /**
     * Return a list of given course info, with the data/time info turned into a hash-map.
     *
     * PRECONDITION:     courseInfo is a list returned by getCourseInfo.
     * @param courseInfo a list of all the course info, scraped on the U of T coursefinder site.
     * @return           A 2-d list where the date/time is hash-mapped to the day and a array of
     *                   start times.
     */
    private static List<List> mapDateTime(List<String> courseInfo){
        List<List<String>> slicedCourseInfo = sliceCourseInfo(courseInfo);
        // Take all values of sliced course info, and change the date time to a hashmap.
        List<List> mappedDateTime = new ArrayList();
        for(List lst: slicedCourseInfo){
            Map<String, int[]> curMappedDateTime = new HashMap<>();
            // Get the current grouped date time, from lst.
            List<List<String>> curGroupedDateTime = groupBrokenString(breakUpDateTime((String)lst.get(1)), 3);
            for(List group: curGroupedDateTime){
                // Initialize current values.
                String curDay = "";
                int[] curTime = new int[2];
                // Set the start/end time to the corresponding grouped value, casted as an int
                curTime[0] = Integer.valueOf((String)group.get(1));
                curTime[1] = Integer.valueOf((String)group.get(2));
                curDay = (String)group.get(0);

                // Add to current hashmap
                curMappedDateTime.put(curDay, curTime);
            }
            // After finished the hashmap for the current lecture section, set the sliced info's
            // date time index to the hashmap, which is always index 2 according U of T format.
            lst.set(1, curMappedDateTime);
            mappedDateTime.add(lst);
        }
        return mappedDateTime;
    }

    /**
     * Returns a list of lectures, given a course.
     * PRECONDITION:     courseInfo is a list returned by getCourseInfo.
     * @param courseInfo a list of all the course info, scraped on the U of T coursefinder site.
     * @return
     */
    private static List<Lecture> instantiateLectures(List<String> courseInfo){
        List<List> mappedCourseInfo = mapDateTime(courseInfo);
        List<Lecture> lectures = new ArrayList<>();
        for(List lst: mappedCourseInfo){
            Lecture curLecture = new Lecture((String)lst.get(0), (HashMap)lst.get(1));
            lectures.add(curLecture);
        }
        return lectures;
    }

    /**
     * Instantiates a course, given a courseCode String.
     * @param courseCode The course to instantiate.
     * @return           A course containing a course code and list of Lectures.
     */
    public static Course instantiateCourse(String courseCode) throws IOException {
        List<String> courseInfo = textScraper.getCourseInfo(courseCode);
        List<Lecture> lectures = instantiateLectures(courseInfo);
        Course newCourse = new Course(courseCode, lectures);
        return newCourse;
    }
}