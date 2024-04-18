# Use an official OpenJDK runtime as a parent image
FROM eclipse-temurin:17-jdk-alpine as build

# Install any needed packages specified in requirements.txt
RUN apk add --no-cache maven

# Set the working directory to /app
WORKDIR /app

# Copy the current directory contents into the container at /app
COPY . /app

# Run the command to build the project
RUN mvn package

# Run the application
CMD ["java", "-jar", "target/SBML2RDF.jar"]