package sec.bftb.server.exceptions;

public enum ErrorMessage {
    
    MESSAGE_INTEGRITY("Message integrity compromissed."),
    SEQUENCE_NUMBER("Invalid Sequence Number, possible replay attack detected."),
    USER_ALREADY_EXISTS("This user already has an account");
    /*INVALID_CREDENTIALS("The username or password are incorrect."),
    INVALID_PASSWORD("The password must have at least 8 characters-"),
    INVALID_USERNAME("The username cannot be empty"),
    INTERNAL_ERROR("There was an error on the server which prevented the operation from being executed."),
    USER_NOT_LOGGED_IN("This user is not logged in."),
    INVALID_SESSION("This session does not contain a valid token/username match."),
    FILE_ALREADY_EXISTS("The file already exists for this user."),
    FILE_DOESNT_EXIST("The file does not exist."),
    FILE_NAME_EMPTY("The name of the file cannot be empty."),
    UNAUTHORIZED_ACCESS("You do not have enough permissions to access this file."),
    UNAUTHORIZED_WRITE("You do not have enough permissions to edit this file."),
    UNAUTHORIZED_FILE_LIST_ACCESS("Only the owner can access shared list of this file."),
    UNAUTHORIZED_FILENAME_CHANGE("You do not have enough permissions to change this file's name."),
    UNAUTHORIZED_FILE_PERMISSIONS("Only the owner can modify permissions of this file."),
    UNAUTHORIZED_FILE_DELETION("You do not have enough permissions to delete this file."),
    USER_ALREADY_EXISTS("The chosen username is not valid, please try another username."), 
    USER_DOESNT_EXIST("This username do not exist, please try another one."), 
    USER_ALREADY_HAVE_PERMISSION("This user already have access to this file."),
    ILLEGAL_MODIFICATION("This file was tampered, send us an email to restore the file.");*/ //TODO->apagar, são exemplos
    
    public final String label;

    ErrorMessage(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return this.label;
    }
}
