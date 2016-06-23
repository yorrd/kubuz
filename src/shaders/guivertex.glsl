#version 330

in vec3 position;
in vec2 texCoord;

out vec2 outTexCoord;

void main() {
    gl_Position = vec4(position, 1);
    outTexCoord = texCoord;
}