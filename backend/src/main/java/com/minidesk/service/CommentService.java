package com.minidesk.service;

import com.minidesk.dto.request.CommentRequest;
import com.minidesk.dto.response.CommentResponse;
import com.minidesk.entity.User;

import java.util.List;

public interface CommentService {

    CommentResponse addComment(Long ticketId, CommentRequest request, User currentUser);

    List<CommentResponse> getCommentsByTicket(Long ticketId, User currentUser);
}
