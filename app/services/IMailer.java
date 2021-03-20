package services;

import java.util.List;

import models.user.User;

public interface IMailer {

    public void retrieveNewEmailMessagesForUser(User user);

}
