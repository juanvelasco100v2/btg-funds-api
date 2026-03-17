package com.btg.fundmanagement.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.util.List;

public final class Requests {
    private Requests() {}

    public record Login(
            @NotBlank String email,
            @NotBlank String password
    ) {}

    public record Register(
            @NotBlank @Email String email,
            @NotBlank String name,
            @NotBlank @Size(min = 8) String password,
            String phone,
            @Pattern(regexp = "EMAIL|SMS") String notificationPreference
    ) {}

    public record Subscribe(
            @NotBlank String fundId
    ) {}

    public record UpdateProfile(
            @Pattern(regexp = "EMAIL|SMS") String notificationPreference,
            String phone
    ) {}

    public record CreateRole(
            @NotBlank String roleId,
            @NotBlank String roleName,
            String description,
            @NotEmpty @Valid List<PermissionEntry> permissions
    ) {}

    public record UpdateRole(
            String roleName,
            String description,
            @Valid List<PermissionEntry> permissions
    ) {}

    public record PermissionEntry(
            @NotBlank String endpoint,
            @NotEmpty List<String> methods
    ) {}

    public record SetupAdmin(
            @NotBlank @Email String email,
            @NotBlank String name,
            @NotBlank @Size(min = 8) String password,
            String phone
    ) {}
}
