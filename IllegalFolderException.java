public class IllegalFolderException extends IllegalArgumentException {

    private String rootFolder;

    /**
     * Public constructor for exception with defined root folder.
     * @param rootFolder
     */
    public IllegalFolderException(String rootFolder) {
        this.rootFolder = rootFolder;
    }

    /**
     * @return Exclude folder.
     */
    public String getRootFolder() {
        return rootFolder;
    }
}
