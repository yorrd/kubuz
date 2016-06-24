#version 150 core

in vec2 pass_TextureCoord;

uniform sampler2D texture_diffuse;

out vec4 out_Color;

void main(void) {
	
	// always use texture
	out_Color = texture(texture_diffuse, pass_TextureCoord);

}