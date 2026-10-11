package com.demandworkbench.decomposition.domain.model;

import com.demandworkbench.decomposition.domain.type.WorkItemType;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class WorkItemNode {
    private final String id;
    private final WorkItemType type;
    private final String name;
    private final String description;
    private final String status;
    private final String role;
    private final String priority;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final String dependency;
    private final List<WorkItemNode> children;

    public WorkItemNode(String id, WorkItemType type, String name, String description, String status,
                        String role, String priority, LocalDate startDate, LocalDate endDate,
                        String dependency, List<WorkItemNode> children) {
        this.id = id;
        this.type = type;
        this.name = name;
        this.description = description;
        this.status = status;
        this.role = role;
        this.priority = priority;
        this.startDate = startDate;
        this.endDate = endDate;
        this.dependency = dependency;
        this.children = children == null ? new ArrayList<>() : new ArrayList<>(children);
    }

    public String id() { return id; }
    public WorkItemType type() { return type; }
    public String name() { return name; }
    public String description() { return description; }
    public String status() { return status; }
    public String role() { return role; }
    public String priority() { return priority; }
    public LocalDate startDate() { return startDate; }
    public LocalDate endDate() { return endDate; }
    public String dependency() { return dependency; }
    public List<WorkItemNode> children() { return children; }
}
