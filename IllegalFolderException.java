/**
 * @class IllegalFolderException
 *
 * Exception which throw for non-root predefined folder.
 */
public class IllegalFolderException extends IllegalArgumentException {

    private String rootFolder;

    /**
     * Public constructor with root folder definition.
     * @param rootFolder
     */
    public IllegalFolderException(String rootFolder) {
        this.rootFolder = rootFolder;
    }

    /**
     * @return Root folder.
     */
    public String getRootFolder() {
        return rootFolder;
    }
}
