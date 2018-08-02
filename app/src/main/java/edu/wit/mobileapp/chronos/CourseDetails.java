package edu.wit.mobileapp.chronos;

import android.os.Bundle;

import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;



public class CourseDetails extends Fragment {


    private String courseNumber;
    private String courseName;
    private String place;
    private String instructor;
    private String time;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_course_details, container, false);

        courseNumber = getArguments().getString("courseNumber");
        courseName = getArguments().getString("courseName");
        place = getArguments().getString("place");
        instructor = getArguments().getString("instructor");
        time = getArguments().getString("time");

        TextView nameTV = v.findViewById(R.id.nameTV);
        TextView numberTV = v.findViewById(R.id.numberTV);
        TextView placeTV = v.findViewById(R.id.placeTV);
        TextView instructorTV = v.findViewById(R.id.instructorTV);
        TextView timeTV = v.findViewById(R.id.timeTV);

        ImageButton closeBTN = v.findViewById(R.id.close);
        closeBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });

        nameTV.setText(courseName);
        numberTV.setText(courseNumber);
        placeTV.setText(place);
        instructorTV.setText(instructor);
        timeTV.setText(time);


        return v;
    }
}

