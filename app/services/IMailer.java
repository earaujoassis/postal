package services;

import java.util.List;

import models.User;

public interface IMailer {

    public void retrieveNewEmailMessagesForUser(User user);

}
