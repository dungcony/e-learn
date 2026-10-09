package com.elearning.learning.repository;

import java.util.UUID;

public interface CourseStudentCount {

    UUID getCourseId();

    long getStudentCount();
}
