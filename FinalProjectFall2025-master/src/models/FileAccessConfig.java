package models;

import java.util.List;
import java.util.Map;

public class FileAccessConfig {

    public static final Map<String, List<String>> FILE_ACCESS_MAP = Map.of(
            "Emp_data.csv",     List.of("aisha.khan@gmail.com", "priya.sharma@gmail.com"),
            "Maintenance.txt",  List.of("rahul.joshi@gmail.com", "vikram.nair@gmail.com"),
            "Proj_Report.txt",  List.of("arjun.patel@gmail.com", "nikhil.verma@gmail.com"),
            "System_Audio.csv", List.of("nikhil.verma@gmail.com", "vikram.nair@gmail.com")
    );
}
