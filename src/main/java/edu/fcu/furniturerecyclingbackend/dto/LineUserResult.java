package edu.fcu.furniturerecyclingbackend.dto;

import edu.fcu.furniturerecyclingbackend.model.AppUsers;

public record LineUserResult(AppUsers user, boolean isMember) { }
