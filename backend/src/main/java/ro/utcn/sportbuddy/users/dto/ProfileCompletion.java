package ro.utcn.sportbuddy.users.dto;

import java.util.List;

public record ProfileCompletion(int percent, List<String> missing) {}